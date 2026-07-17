package net.akive.api.auth.application.service.emailverification;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.application.dto.command.emailverification.EnsureEmailVerifiedCommand;
import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCommand;
import net.akive.api.auth.application.dto.command.emailverification.VerifyEmailVerificationCodeCommand;
import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;
import net.akive.api.auth.application.port.out.emailverification.SaveEmailVerificationPort;
import net.akive.api.auth.application.port.out.emailverification.SendEmailVerificationMailPort;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.auth.exception.EmailVerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EXPIRE_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.RESEND_INTERVAL;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFIED_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.auth.exception.EmailVerificationErrorCode.NOT_FOUND;
import static net.akive.api.auth.exception.EmailVerificationErrorCode.NOT_VERIFIED;
import static net.akive.api.auth.exception.EmailVerificationErrorCode.RESEND_TOO_SOON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final EmailVerificationCode VERIFICATION_CODE = EmailVerificationCode.of("123456");
    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2025, 5, 21, 10, 0);

    @Mock
    SendEmailVerificationMailPort sendEmailVerificationMailPort;

    @Mock
    SaveEmailVerificationPort saveEmailVerificationPort;

    @Mock
    EmailVerificationDomainService emailVerificationDomainService;

    @InjectMocks
    EmailVerificationService emailVerificationService;

    @Test
    void 이메일_인증번호를_발급하고_전송한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, REQUESTED_AT);
        given(emailVerificationDomainService.issue(Email.of(EMAIL), SIGN_UP))
                .willReturn(emailVerification);

        // when
        SendEmailVerificationResult result = emailVerificationService.send(sendCommand(SIGN_UP));

        // then
        assertThat(result.email()).isEqualTo(Email.of(EMAIL));
        assertThat(result.expiresAt()).isEqualTo(REQUESTED_AT.plus(EXPIRE_DURATION));
        assertThat(result.expireSeconds()).isEqualTo(EXPIRE_DURATION.toSeconds());
        assertThat(result.resendIntervalSeconds()).isEqualTo(RESEND_INTERVAL.toSeconds());

        then(emailVerificationDomainService).should().issue(Email.of(EMAIL), SIGN_UP);
        then(sendEmailVerificationMailPort).should().send(emailVerification, EXPIRE_DURATION);
        then(saveEmailVerificationPort).should().save(emailVerification, EXPIRE_DURATION);
    }

    @Test
    void 인증번호_발급에_실패하면_전송하지_않고_저장하지_않는다() {
        // given
        given(emailVerificationDomainService.issue(Email.of(EMAIL), SIGN_UP))
                .willThrow(new EmailVerificationException(RESEND_TOO_SOON));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerificationService.send(sendCommand(SIGN_UP)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(RESEND_TOO_SOON);

        then(sendEmailVerificationMailPort).should(never()).send(any(), any());
        then(saveEmailVerificationPort).should(never()).save(any(), any());
    }

    @Test
    void 인증번호를_검증하고_인증정보를_저장한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, REQUESTED_AT);
        given(emailVerificationDomainService.verify(Email.of(EMAIL), SIGN_UP, VERIFICATION_CODE))
                .willReturn(emailVerification);

        // when
        emailVerificationService.verify(verifyCommand(SIGN_UP, VERIFICATION_CODE.value()));

        // then
        then(emailVerificationDomainService).should().verify(Email.of(EMAIL), SIGN_UP, VERIFICATION_CODE);
        then(saveEmailVerificationPort).should().save(emailVerification, VERIFIED_EXPIRATION_DURATION);
    }

    @Test
    void 인증번호_검증에_실패하면_인증정보를_저장하지_않는다() {
        // given
        given(emailVerificationDomainService.verify(Email.of(EMAIL), SIGN_UP, VERIFICATION_CODE))
                .willThrow(new EmailVerificationException(NOT_FOUND));

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerificationService.verify(verifyCommand(SIGN_UP, VERIFICATION_CODE.value())))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(NOT_FOUND);

        then(saveEmailVerificationPort).should(never()).save(any(), any());
    }

    @Test
    void 인증이_완료된_이메일이면_인증_완료_확인에_성공한다() {
        // when
        emailVerificationService.ensureVerified(ensureVerifiedCommand(SIGN_UP));

        // then
        then(emailVerificationDomainService).should().ensureVerified(Email.of(EMAIL), SIGN_UP);
    }

    @Test
    void 인증_완료_확인에_실패하면_예외를_던진다() {
        // given
        willThrow(new EmailVerificationException(NOT_VERIFIED))
                .given(emailVerificationDomainService)
                .ensureVerified(Email.of(EMAIL), SIGN_UP);

        // when & then
        assertThatExceptionOfType(EmailVerificationException.class)
                .isThrownBy(() -> emailVerificationService.ensureVerified(ensureVerifiedCommand(SIGN_UP)))
                .extracting(EmailVerificationException::getErrorCode)
                .isEqualTo(NOT_VERIFIED);

        then(emailVerificationDomainService).should().ensureVerified(Email.of(EMAIL), SIGN_UP);
    }

    private SendEmailVerificationCommand sendCommand(EmailVerificationPurpose verificationPurpose) {
        return new SendEmailVerificationCommand(Email.of(EMAIL), verificationPurpose);
    }

    private VerifyEmailVerificationCodeCommand verifyCommand(
            EmailVerificationPurpose verificationPurpose,
            String verificationCode
    ) {
        return new VerifyEmailVerificationCodeCommand(
                Email.of(EMAIL),
                verificationPurpose,
                EmailVerificationCode.of(verificationCode)
        );
    }

    private EnsureEmailVerifiedCommand ensureVerifiedCommand(EmailVerificationPurpose verificationPurpose) {
        return new EnsureEmailVerifiedCommand(Email.of(EMAIL), verificationPurpose);
    }

    private EmailVerification emailVerification(
            EmailVerificationPurpose verificationPurpose,
            LocalDateTime requestedAt
    ) {
        return EmailVerification.create(Email.of(EMAIL), verificationPurpose, VERIFICATION_CODE, requestedAt);
    }
}
