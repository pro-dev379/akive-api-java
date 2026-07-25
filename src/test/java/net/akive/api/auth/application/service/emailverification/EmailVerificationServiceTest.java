package net.akive.api.auth.application.service.emailverification;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.common.exception.business.TooManyRequestsException;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRE_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EMAIL_VERIFICATION_RESEND_MIN_INTERVAL;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFIED_EMAIL_VERIFICATION_EXPIRE_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.NotFoundError.EMAIL_VERIFICATION_NOT_FOUND;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.TooManyRequestsError.EMAIL_VERIFICATION_RESEND_TOO_FREQUENT;
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
    private static final EmailVerificationCode VERIFICATION_CODE = new EmailVerificationCode("123456");
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
        given(emailVerificationDomainService.issue(new Email(EMAIL), SIGN_UP))
                .willReturn(emailVerification);

        // when
        SendEmailVerificationResult result = emailVerificationService.send(sendCommand(SIGN_UP));

        // then
        assertThat(result.email()).isEqualTo(new Email(EMAIL));
        assertThat(result.expiresAt()).isEqualTo(REQUESTED_AT.plus(VERIFICATION_CODE_EXPIRE_DURATION));
        assertThat(result.expireSeconds()).isEqualTo(VERIFICATION_CODE_EXPIRE_DURATION.toSeconds());
        assertThat(result.resendIntervalSeconds()).isEqualTo(EMAIL_VERIFICATION_RESEND_MIN_INTERVAL.toSeconds());

        then(emailVerificationDomainService).should().issue(new Email(EMAIL), SIGN_UP);
        then(sendEmailVerificationMailPort).should().send(emailVerification, VERIFICATION_CODE_EXPIRE_DURATION);
        then(saveEmailVerificationPort).should().save(emailVerification, VERIFICATION_CODE_EXPIRE_DURATION);
    }

    @Test
    void 인증번호_발급에_실패하면_전송하지_않고_저장하지_않는다() {
        // given
        given(emailVerificationDomainService.issue(new Email(EMAIL), SIGN_UP))
                .willThrow(new TooManyRequestsException(EMAIL_VERIFICATION_RESEND_TOO_FREQUENT));

        // when & then
        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> emailVerificationService.send(sendCommand(SIGN_UP)))
                .extracting(TooManyRequestsException::getError)
                .isEqualTo(EMAIL_VERIFICATION_RESEND_TOO_FREQUENT);

        then(sendEmailVerificationMailPort).should(never()).send(any(), any());
        then(saveEmailVerificationPort).should(never()).save(any(), any());
    }

    @Test
    void 인증번호를_검증하고_인증정보를_저장한다() {
        // given
        EmailVerification emailVerification = emailVerification(SIGN_UP, REQUESTED_AT);
        given(emailVerificationDomainService.verify(new Email(EMAIL), SIGN_UP, VERIFICATION_CODE))
                .willReturn(emailVerification);

        // when
        emailVerificationService.verify(verifyCommand(SIGN_UP, VERIFICATION_CODE.value()));

        // then
        then(emailVerificationDomainService).should().verify(new Email(EMAIL), SIGN_UP, VERIFICATION_CODE);
        then(saveEmailVerificationPort).should().save(emailVerification, VERIFIED_EMAIL_VERIFICATION_EXPIRE_DURATION);
    }

    @Test
    void 인증번호_검증에_실패하면_인증정보를_저장하지_않는다() {
        // given
        given(emailVerificationDomainService.verify(new Email(EMAIL), SIGN_UP, VERIFICATION_CODE))
                .willThrow(new NotFoundException(EMAIL_VERIFICATION_NOT_FOUND));

        // when & then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> emailVerificationService.verify(verifyCommand(SIGN_UP, VERIFICATION_CODE.value())))
                .extracting(NotFoundException::getError)
                .isEqualTo(EMAIL_VERIFICATION_NOT_FOUND);

        then(saveEmailVerificationPort).should(never()).save(any(), any());
    }

    @Test
    void 인증이_완료된_이메일이면_인증_완료_확인에_성공한다() {
        // when
        emailVerificationService.ensureVerified(ensureVerifiedCommand(SIGN_UP));

        // then
        then(emailVerificationDomainService).should().ensureVerified(new Email(EMAIL), SIGN_UP);
    }

    @Test
    void 인증_완료_확인에_실패하면_예외를_던진다() {
        // given
        willThrow(new BadRequestException(EMAIL_NOT_VERIFIED))
                .given(emailVerificationDomainService)
                .ensureVerified(new Email(EMAIL), SIGN_UP);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> emailVerificationService.ensureVerified(ensureVerifiedCommand(SIGN_UP)))
                .extracting(BadRequestException::getError)
                .isEqualTo(EMAIL_NOT_VERIFIED);

        then(emailVerificationDomainService).should().ensureVerified(new Email(EMAIL), SIGN_UP);
    }

    private SendEmailVerificationCommand sendCommand(EmailVerificationPurpose verificationPurpose) {
        return new SendEmailVerificationCommand(new Email(EMAIL), verificationPurpose);
    }

    private VerifyEmailVerificationCodeCommand verifyCommand(
            EmailVerificationPurpose verificationPurpose,
            String verificationCode
    ) {
        return new VerifyEmailVerificationCodeCommand(
                new Email(EMAIL),
                verificationPurpose,
                new EmailVerificationCode(verificationCode)
        );
    }

    private EnsureEmailVerifiedCommand ensureVerifiedCommand(EmailVerificationPurpose verificationPurpose) {
        return new EnsureEmailVerifiedCommand(new Email(EMAIL), verificationPurpose);
    }

    private EmailVerification emailVerification(
            EmailVerificationPurpose verificationPurpose,
            LocalDateTime requestedAt
    ) {
        return EmailVerification.create(new Email(EMAIL), verificationPurpose, VERIFICATION_CODE, requestedAt);
    }
}
