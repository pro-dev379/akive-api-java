package net.akive.api.auth.email.application;

import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;
import net.akive.api.auth.email.application.port.out.EmailAuthCodeGenerator;
import net.akive.api.auth.email.application.port.out.EmailAuthMailSender;
import net.akive.api.auth.email.application.port.out.EmailAuthRepository;
import net.akive.api.auth.email.application.port.out.EmailAuthUserValidator;
import net.akive.api.auth.email.domain.EmailAuth;
import net.akive.api.auth.email.domain.EmailAuthPurpose;
import net.akive.api.auth.email.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static net.akive.api.auth.email.domain.EmailAuthPolicy.EXPIRE_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest {

    @Mock
    EmailAuthMailSender emailAuthMailSender;

    @Mock
    EmailAuthCodeGenerator emailAuthCodeGenerator;

    @Mock
    EmailAuthUserValidator emailAuthUserValidator;

    @Mock
    EmailAuthRepository emailAuthRepository;

    Clock clock;

    EmailAuthService emailAuthService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2025-05-21T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        emailAuthService = new EmailAuthService(
                emailAuthMailSender,
                emailAuthCodeGenerator,
                emailAuthUserValidator,
                emailAuthRepository,
                clock
        );
    }

    @Test
    void 회원_가입_이메일_인증_번호를_전송한다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        String authCode = "123456";

        given(emailAuthCodeGenerator.generate()).willReturn(authCode);
        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.empty());

        // when
        EmailAuthCodeSendResult result = emailAuthService.sendAuthCode(email, authPurpose);

        // then
        then(emailAuthUserValidator)
                .should()
                .validateEmailNotDuplicate(email);

        then(emailAuthUserValidator)
                .should(never())
                .validateUserExistsByEmail(any());

        then(emailAuthMailSender)
                .should()
                .sendAuthCode(eq(email), eq(authCode), any(Duration.class));

        then(emailAuthRepository)
                .should()
                .save(any(EmailAuth.class), any(Duration.class));

        assertThat(result.email()).isEqualTo(email);
        assertThat(result.expireSeconds()).isEqualTo(300);
        assertThat(result.resendIntervalSeconds()).isEqualTo(30);
    }

    @Test
    void 비밀번호_찾기_이메일_인증_번호를_전송한다() {
        // given
        String email = "test@test.com";
        String authCode = "123456";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.FIND_PASSWORD;

        given(emailAuthCodeGenerator.generate()).willReturn(authCode);
        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.empty());

        // when
        EmailAuthCodeSendResult result = emailAuthService.sendAuthCode(email, authPurpose);

        // then
        then(emailAuthUserValidator)
                .should()
                .validateUserExistsByEmail(email);

        then(emailAuthUserValidator)
                .should(never())
                .validateEmailNotDuplicate(any());

        then(emailAuthMailSender)
                .should()
                .sendAuthCode(eq(email), eq(authCode), any(Duration.class));

        then(emailAuthRepository)
                .should()
                .save(any(EmailAuth.class), any(Duration.class));

        assertThat(result.email()).isEqualTo(email);
        assertThat(result.expireSeconds()).isEqualTo(300);
        assertThat(result.resendIntervalSeconds()).isEqualTo(30);
    }

    @Test
    void 기존_인증_정보가_있으면_갱신해서_전송한다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        String authCode = "123456";
        EmailAuth previous = EmailAuth.create(
                email,
                authPurpose,
                "000000",
                LocalDateTime.of(2025, 5, 21, 9, 55)
        );

        given(emailAuthCodeGenerator.generate()).willReturn(authCode);
        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.of(previous));

        // when
        emailAuthService.sendAuthCode(email, authPurpose);

        // then
        then(emailAuthRepository)
                .should()
                .save(argThat(emailAuth ->
                        emailAuth.getAuthCode().equals(authCode)
                ), any(Duration.class));
    }

    @Test
    void 인증_번호_재전송_가능_시간_이전이면_예외가_발생하고_저장하지_않는다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;

        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(
                    Optional.of(
                        EmailAuth.create(email, authPurpose, "123456", LocalDateTime.now(clock))
                    )
                );

        // when & then
        assertThatThrownBy(() -> emailAuthService.sendAuthCode(email, authPurpose))
                .isInstanceOf(EmailAuthResendTooSoonException.class);

        then(emailAuthRepository)
                .should(never())
                .save(any(), any());

        then(emailAuthMailSender)
                .should(never())
                .sendAuthCode(any(), any(), any());
    }

    @Test
    void 인증_번호_재전송_횟수를_초과하면_메일을_전송하지_않고_저장하지_않는다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        LocalDateTime requestedAt = LocalDateTime.now(clock);
        EmailAuth emailAuth = EmailAuth.create(email, authPurpose, "123456", requestedAt);

        emailAuth.renew("111111", requestedAt.plusMinutes(1));
        emailAuth.renew("222222", requestedAt.plusMinutes(2));
        emailAuth.renew("333333", requestedAt.plusMinutes(3));

        given(emailAuthCodeGenerator.generate()).willReturn("123456");
        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.of(emailAuth));

        // when & then
        assertThatThrownBy(() -> emailAuthService.sendAuthCode(email, authPurpose))
                .isInstanceOf(EmailAuthResendLimitExceededException.class);

        then(emailAuthMailSender)
                .should(never())
                .sendAuthCode(any(), any(), any());

        then(emailAuthRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 이메일_인증번호를_검증한다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        String authCode = "123456";

        LocalDateTime requestedAt = LocalDateTime.now(clock).minusMinutes(1);
        EmailAuth emailAuth = EmailAuth.create(
                email,
                authPurpose,
                authCode,
                requestedAt
        );

        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.of(emailAuth));

        // when
        emailAuthService.verifyAuthCode(email, authPurpose, authCode);

        // then
        then(emailAuthRepository)
                .should()
                .save(
                        argThat(savedEmailAuth ->
                                savedEmailAuth.isVerified()
                                        && savedEmailAuth.getEmail().equals(email)
                                        && savedEmailAuth.getAuthPurpose() == authPurpose
                        ),
                        eq(Duration.ofMinutes(15))
                );
    }

    @Test
    void 이메일_인증_정보가_없으면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        String authCode = "123456";

        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                emailAuthService.verifyAuthCode(email, authPurpose, authCode)
        ).isInstanceOf(EmailAuthNotFoundException.class);

        then(emailAuthRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 이메일_인증번호가_일치하지_않으면_예외가_발생하고_저장하지_않는다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;

        EmailAuth emailAuth = EmailAuth.create(
                email,
                authPurpose,
                "123456",
                LocalDateTime.now(clock).minusMinutes(1)
        );

        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.of(emailAuth));

        // when & then
        assertThatThrownBy(() ->
                emailAuthService.verifyAuthCode(email, authPurpose, "000000")
        ).isInstanceOf(EmailAuthCodeMismatchException.class);

        then(emailAuthRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 이메일_인증번호가_만료되면_예외가_발생하고_저장하지_않는다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        String authCode = "123456";

        EmailAuth emailAuth = EmailAuth.create(
                email,
                authPurpose,
                authCode,
                LocalDateTime.now(clock).minus(EXPIRE_DURATION).minusSeconds(1)
        );

        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.of(emailAuth));

        // when & then
        assertThatThrownBy(() ->
                emailAuthService.verifyAuthCode(email, authPurpose, authCode)
        ).isInstanceOf(EmailAuthExpiredException.class);

        then(emailAuthRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 이미_인증된_경우_예외가_발생하고_저장하지_않는다() {
        // given
        String email = "test@test.com";
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        String authCode = "123456";

        EmailAuth emailAuth = EmailAuth.create(
                email,
                authPurpose,
                authCode,
                LocalDateTime.now(clock).minusMinutes(2)
        );
        emailAuth.verify(authCode, LocalDateTime.now(clock).minusMinutes(1));

        given(emailAuthRepository.findByEmailAndPurpose(email, authPurpose))
                .willReturn(Optional.of(emailAuth));

        // when & then
        assertThatThrownBy(() ->
                emailAuthService.verifyAuthCode(email, authPurpose, authCode)
        ).isInstanceOf(EmailAuthAlreadyVerifiedException.class);

        then(emailAuthRepository)
                .should(never())
                .save(any(), any());
    }
}