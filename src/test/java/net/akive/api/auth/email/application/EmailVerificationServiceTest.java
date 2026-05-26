package net.akive.api.auth.email.application;

import net.akive.api.auth.email.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.email.application.port.out.EmailVerificationCodeGenerator;
import net.akive.api.auth.email.application.port.out.EmailVerificationMailSender;
import net.akive.api.auth.email.application.port.out.EmailVerificationRepository;
import net.akive.api.auth.email.application.port.out.EmailVerificationUserValidator;
import net.akive.api.auth.email.domain.EmailVerification;
import net.akive.api.auth.email.domain.EmailVerificationPurpose;
import net.akive.api.auth.email.domain.exception.*;
import net.akive.api.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static net.akive.api.auth.email.domain.EmailVerificationPolicy.EXPIRE_DURATION;
import static net.akive.api.auth.email.domain.exception.EmailVerificationErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    EmailVerificationMailSender emailVerificationMailSender;

    @Mock
    EmailVerificationCodeGenerator emailVerificationCodeGenerator;

    @Mock
    EmailVerificationUserValidator emailVerificationUserValidator;

    @Mock
    EmailVerificationRepository emailVerificationRepository;

    Clock clock;

    EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2025-05-21T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        emailVerificationService = new EmailVerificationService(
                emailVerificationMailSender,
                emailVerificationCodeGenerator,
                emailVerificationUserValidator,
                emailVerificationRepository,
                clock
        );
    }

    @Test
    void 회원가입_이메일_인증번호_요청시_인증번호를_전송한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        String verificationCode = "123456";

        given(emailVerificationCodeGenerator.generate()).willReturn(verificationCode);
        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.empty());

        // when
        EmailVerificationCodeSendResult result = emailVerificationService.send(email, verificationPurpose);

        // then
        then(emailVerificationUserValidator)
                .should()
                .validateEmailNotDuplicate(email);

        then(emailVerificationUserValidator)
                .should(never())
                .validateUserExistsByEmail(any());

        then(emailVerificationMailSender)
                .should()
                .send(eq(email), eq(verificationCode), any(Duration.class));

        then(emailVerificationRepository)
                .should()
                .save(any(EmailVerification.class), any(Duration.class));

        assertThat(result.email()).isEqualTo(email);
        assertThat(result.expireSeconds()).isEqualTo(300);
        assertThat(result.resendIntervalSeconds()).isEqualTo(30);
    }

    @Test
    void 비밀번호찾기_이메일_인증번호_요청시_인증번호를_전송한다() {
        // given
        String email = "test@test.com";
        String verificationCode = "123456";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.FIND_PASSWORD;

        given(emailVerificationCodeGenerator.generate()).willReturn(verificationCode);
        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.empty());

        // when
        EmailVerificationCodeSendResult result = emailVerificationService.send(email, verificationPurpose);

        // then
        then(emailVerificationUserValidator)
                .should()
                .validateUserExistsByEmail(email);

        then(emailVerificationUserValidator)
                .should(never())
                .validateEmailNotDuplicate(any());

        then(emailVerificationMailSender)
                .should()
                .send(eq(email), eq(verificationCode), any(Duration.class));

        then(emailVerificationRepository)
                .should()
                .save(any(EmailVerification.class), any(Duration.class));

        assertThat(result.email()).isEqualTo(email);
        assertThat(result.expireSeconds()).isEqualTo(300);
        assertThat(result.resendIntervalSeconds()).isEqualTo(30);
    }

    @Test
    void 기존_인증정보가_존재하면_새_인증정보로_갱신하고_인증번호를_전송한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        String verificationCode = "123456";
        EmailVerification previous = EmailVerification.create(
                email,
                verificationPurpose,
                "000000",
                LocalDateTime.of(2025, 5, 21, 9, 55)
        );

        given(emailVerificationCodeGenerator.generate()).willReturn(verificationCode);
        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(previous));

        // when
        emailVerificationService.send(email, verificationPurpose);

        // then
        then(emailVerificationMailSender)
                .should()
                .send(eq(email), eq(verificationCode), any(Duration.class));

        then(emailVerificationRepository)
                .should()
                .save(
                    argThat(
                        emailVerification ->
                            emailVerification == previous &&
                            emailVerification.getVerificationCode().equals(verificationCode) &&
                            !emailVerification.isVerified()
                    ),
                    any(Duration.class)
                );
    }

    @Test
    void 재전송_가능시간_이전에_인증번호를_요청하면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;

        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(
                    Optional.of(
                        EmailVerification.create(email, verificationPurpose, "123456", LocalDateTime.now(clock))
                    )
                );

        // when & then
        assertThatThrownBy(() -> emailVerificationService.send(email, verificationPurpose))
                .satisfies(e -> assertErrorCode(e, RESEND_TOO_SOON));

        then(emailVerificationRepository)
                .should(never())
                .save(any(), any());

        then(emailVerificationMailSender)
                .should(never())
                .send(any(), any(), any());
    }

    @Test
    void 재전송_횟수를_초과하면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        LocalDateTime requestedAt = LocalDateTime.now(clock);
        EmailVerification emailVerification = EmailVerification.create(email, verificationPurpose, "123456", requestedAt);

        emailVerification.renew("111111", requestedAt.plusMinutes(1));
        emailVerification.renew("222222", requestedAt.plusMinutes(2));
        emailVerification.renew("333333", requestedAt.plusMinutes(3));

        given(emailVerificationCodeGenerator.generate()).willReturn("123456");
        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.send(email, verificationPurpose))
                .satisfies(e -> assertErrorCode(e, RESEND_LIMIT_EXCEEDED));

        then(emailVerificationMailSender)
                .should(never())
                .send(any(), any(), any());

        then(emailVerificationRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 유효한_인증번호를_입력하면_이메일_인증을_완료한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        String verificationCode = "123456";

        LocalDateTime requestedAt = LocalDateTime.now(clock).minusMinutes(1);
        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                verificationCode,
                requestedAt
        );

        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when
        emailVerificationService.verify(email, verificationPurpose, verificationCode);

        // then
        then(emailVerificationRepository)
                .should()
                .save(
                        argThat(savedEmailAuth ->
                                savedEmailAuth.isVerified()
                                        && savedEmailAuth.getEmail().equals(email)
                                        && savedEmailAuth.getVerificationPurpose() == verificationPurpose
                        ),
                        eq(Duration.ofMinutes(15))
                );
    }

    @Test
    void 인증번호_검증시_이메일_인증정보가_없으면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        String verificationCode = "123456";

        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verify(email, verificationPurpose, verificationCode))
                .satisfies(e -> assertErrorCode(e, NOT_FOUND));

        then(emailVerificationRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 인증번호가_일치하지_않으면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;

        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                "123456",
                LocalDateTime.now(clock).minusMinutes(1)
        );

        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verify(email, verificationPurpose, "000000"))
                .satisfies(e -> assertErrorCode(e, CODE_MISMATCH));

        then(emailVerificationRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 인증번호가_만료되면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        String verificationCode = "123456";

        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                verificationCode,
                LocalDateTime.now(clock).minus(EXPIRE_DURATION).minusSeconds(1)
        );

        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verify(email, verificationPurpose, verificationCode))
                .satisfies(e -> assertErrorCode(e, EXPIRED));

        then(emailVerificationRepository)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 이미_인증이_완료된_경우_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        String verificationCode = "123456";

        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                verificationCode,
                LocalDateTime.now(clock).minusMinutes(2)
        );
        emailVerification.verify(verificationCode, LocalDateTime.now(clock).minusMinutes(1));

        given(emailVerificationRepository.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verify(email, verificationPurpose, verificationCode))
                .satisfies(e -> assertErrorCode(e, ALREADY_VERIFIED));

        then(emailVerificationRepository)
                .should(never())
                .save(any(), any());
    }

    private void assertErrorCode(Throwable throwable, ErrorCode expected) {
        assertThat(throwable)
                .isInstanceOf(EmailVerificationException.class);

        assertThat(((EmailVerificationException) throwable).getErrorCode())
                .isEqualTo(expected);
    }
}