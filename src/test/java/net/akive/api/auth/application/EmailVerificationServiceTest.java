package net.akive.api.auth.application;

import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeSendCommand;
import net.akive.api.auth.application.dto.result.EmailVerificationCodeSendResult;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.port.out.emailverification.*;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.common.exception.ErrorCode;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import net.akive.api.user.application.port.in.ValidateUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EXPIRE_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFIED_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.*;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    SendEmailVerificationMailPort sendEmailVerificationMailPort;

    @Mock
    GenerateEmailVerificationCodePort generateEmailVerificationCodePort;

    @Mock
    ValidateUserUseCase validateUserUseCase;

    @Mock
    LoadEmailVerificationPort loadEmailVerificationPort;

    @Mock
    SaveEmailVerificationPort saveEmailVerificationPort;

    CurrentTimeProvider currentTimeProvider;

    EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(Instant.parse("2025-05-21T01:00:00Z"));

        emailVerificationService = new EmailVerificationService(
                validateUserUseCase,
                sendEmailVerificationMailPort,
                generateEmailVerificationCodePort,
                loadEmailVerificationPort,
                saveEmailVerificationPort,
                currentTimeProvider
        );
    }

    @Test
    void 회원가입_이메일_인증번호_요청시_인증번호를_전송한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        String verificationCode = "123456";

        given(generateEmailVerificationCodePort.generate()).willReturn(verificationCode);
        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.empty());

        // when
        EmailVerificationCodeSendResult result
                = emailVerificationService.send(EmailVerificationCodeSendCommand.of(email, verificationPurpose));

        // then
        then(validateUserUseCase)
                .should()
                .validateEmailNotDuplicate(email);

        then(validateUserUseCase)
                .should(never())
                .validateUserExistsByEmail(any());

        then(sendEmailVerificationMailPort)
                .should()
                .send(eq(email), eq(verificationCode), any(Duration.class));

        then(saveEmailVerificationPort)
                .should()
                .save(any(EmailVerification.class), any(Duration.class));

        assertThat(result.email()).isEqualTo(email);
        assertThat(result.expireSeconds()).isEqualTo(300);
        assertThat(result.resendIntervalSeconds()).isEqualTo(30);
    }

    @Test
    void 패스워드_찾기_이메일_인증번호_요청시_인증번호를_전송한다() {
        // given
        String email = "test@test.com";
        String verificationCode = "123456";
        EmailVerificationPurpose verificationPurpose = FIND_PASSWORD;

        given(generateEmailVerificationCodePort.generate()).willReturn(verificationCode);
        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.empty());

        // when
        EmailVerificationCodeSendResult result
                = emailVerificationService.send(EmailVerificationCodeSendCommand.of(email, verificationPurpose));

        // then
        then(validateUserUseCase)
                .should()
                .validateUserExistsByEmail(email);

        then(validateUserUseCase)
                .should(never())
                .validateEmailNotDuplicate(any());

        then(sendEmailVerificationMailPort)
                .should()
                .send(eq(email), eq(verificationCode), any(Duration.class));

        then(saveEmailVerificationPort)
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
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        String verificationCode = "123456";
        EmailVerification previous = EmailVerification.create(
                email,
                verificationPurpose,
                "000000",
                LocalDateTime.of(2025, 5, 21, 9, 55)
        );

        given(generateEmailVerificationCodePort.generate()).willReturn(verificationCode);
        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(previous));

        // when
        emailVerificationService.send(EmailVerificationCodeSendCommand.of(email, verificationPurpose));

        // then
        then(sendEmailVerificationMailPort)
                .should()
                .send(eq(email), eq(verificationCode), any(Duration.class));

        then(saveEmailVerificationPort)
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
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(
                    Optional.of(
                        EmailVerification.create(email, verificationPurpose, "123456", currentTimeProvider.now())
                    )
                );

        // when & then
        assertThatThrownBy(() -> emailVerificationService.send(EmailVerificationCodeSendCommand.of(email, verificationPurpose)))
                .satisfies(e -> assertErrorCode(e, RESEND_TOO_SOON));

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());

        then(sendEmailVerificationMailPort)
                .should(never())
                .send(any(), any(), any());
    }

    @Test
    void 재전송_횟수를_초과하면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        LocalDateTime requestedAt = currentTimeProvider.now();
        EmailVerification emailVerification = EmailVerification.create(email, verificationPurpose, "123456", requestedAt);

        emailVerification.renew("111111", requestedAt.plusMinutes(1));
        emailVerification.renew("222222", requestedAt.plusMinutes(2));
        emailVerification.renew("333333", requestedAt.plusMinutes(3));

        given(generateEmailVerificationCodePort.generate()).willReturn("123456");
        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.send(EmailVerificationCodeSendCommand.of(email, verificationPurpose)))
                .satisfies(e -> assertErrorCode(e, RESEND_LIMIT_EXCEEDED));

        then(sendEmailVerificationMailPort)
                .should(never())
                .send(any(), any(), any());

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 유효한_인증번호를_입력하면_이메일_인증을_완료한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        String verificationCode = "123456";

        LocalDateTime requestedAt = currentTimeProvider.now().minusMinutes(1);
        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                verificationCode,
                requestedAt
        );

        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when
        emailVerificationService.verify(EmailVerificationCodeVerifyCommand.of(email, verificationPurpose, verificationCode));

        // then
        then(saveEmailVerificationPort)
                .should()
                .save(
                        argThat(savedEmailAuth ->
                                savedEmailAuth.isVerified()
                                        && savedEmailAuth.getEmail().equals(email)
                                        && savedEmailAuth.getVerificationPurpose() == verificationPurpose
                        ),
                        eq(VERIFIED_EXPIRATION_DURATION)
                );
    }

    @Test
    void 인증번호_검증시_이메일_인증정보가_없으면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        String verificationCode = "123456";

        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                emailVerificationService.verify(
                        EmailVerificationCodeVerifyCommand.of(email, verificationPurpose, verificationCode)
                    )
                )
                .satisfies(e -> assertErrorCode(e, NOT_FOUND));

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 인증번호가_일치하지_않으면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;

        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                "123456",
                currentTimeProvider.now().minusMinutes(1)
        );

        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() ->
                emailVerificationService.verify(
                        EmailVerificationCodeVerifyCommand.of(email, verificationPurpose, "000000")
                    )
                )
                .satisfies(e -> assertErrorCode(e, CODE_MISMATCH));

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 인증번호가_만료되면_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        String verificationCode = "123456";

        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                verificationCode,
                currentTimeProvider.now().minus(EXPIRE_DURATION).minusSeconds(1)
        );

        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() ->
                emailVerificationService.verify(
                        EmailVerificationCodeVerifyCommand.of(email, verificationPurpose, verificationCode)
                    )
                )
                .satisfies(e -> assertErrorCode(e, EXPIRED));

        then(saveEmailVerificationPort)
                .should(never())
                .save(any(), any());
    }

    @Test
    void 이미_인증이_완료된_경우_예외가_발생한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        String verificationCode = "123456";

        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                verificationCode,
                currentTimeProvider.now().minusMinutes(2)
        );
        emailVerification.verify(verificationCode, currentTimeProvider.now().minusMinutes(1));

        given(loadEmailVerificationPort.findByEmailAndVerificationPurpose(email, verificationPurpose))
                .willReturn(Optional.of(emailVerification));

        // when & then
        assertThatThrownBy(() ->
                emailVerificationService.verify(
                        EmailVerificationCodeVerifyCommand.of(email, verificationPurpose, verificationCode)
                    )
                )
                .satisfies(e -> assertErrorCode(e, ALREADY_VERIFIED));

        then(saveEmailVerificationPort)
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