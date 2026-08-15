package net.akive.api.auth.application.emailverification.service;

import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerifiedCheckCommand;
import net.akive.api.auth.application.emailverification.required.EmailVerificationSender;
import net.akive.api.auth.application.emailverification.required.EmailVerificationStore;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.*;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFIED_EMAIL_VERIFICATION_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_VERIFIED;
import static net.akive.api.common.exception.business.TooManyRequestsError.EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    EmailVerificationSender emailVerificationSender;

    @Mock
    EmailVerificationStore emailVerificationStore;

    @Mock
    EmailVerificationDomainService emailVerificationDomainService;

    @InjectMocks
    EmailVerificationService emailVerificationService;

    @Test
    void 이메일_인증번호를_발급하고_전송한다() {
        EmailVerification emailVerification = createEmailVerification();
        given(emailVerificationDomainService.issue(EMAIL, SIGN_UP))
                .willReturn(emailVerification);

        EmailVerification result = emailVerificationService.issue(createIssueCommand());

        assertThat(result).isSameAs(emailVerification);

        then(emailVerificationDomainService).should().issue(EMAIL, SIGN_UP);
        then(emailVerificationSender).should().send(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);
        then(emailVerificationStore).should().save(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);
    }

    @Test
    void 이메일_인증번호_발급을_완료하지_못하면_전송하지_않고_저장하지_않는다() {
        TooManyRequestsException exception = new TooManyRequestsException(EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT);
        given(emailVerificationDomainService.issue(EMAIL, SIGN_UP)).willThrow(exception);

        assertThatThrownBy(() -> emailVerificationService.issue(createIssueCommand())).isSameAs(exception);

        then(emailVerificationDomainService).should().issue(EMAIL, SIGN_UP);
        then(emailVerificationSender).should(never()).send(any(), any());
        then(emailVerificationStore).should(never()).save(any(), any());
    }

    @Test
    void 이메일_인증번호를_검증하고_인증정보를_저장한다() {
        EmailVerification emailVerification = createEmailVerification();
        given(emailVerificationDomainService.verify(EMAIL, SIGN_UP, VERIFICATION_CODE)).willReturn(emailVerification);

        emailVerificationService.verify(CreateVerifyCommand());

        then(emailVerificationDomainService).should().verify(EMAIL, SIGN_UP, VERIFICATION_CODE);
        then(emailVerificationStore).should()
                .save(emailVerification, VERIFIED_EMAIL_VERIFICATION_EXPIRATION_DURATION);
    }

    @Test
    void 이메일_인증번호_검증을_완료하지_못하면_인증정보를_저장하지_않는다() {
        ConflictException exception = new ConflictException(EMAIL_ALREADY_VERIFIED);
        given(emailVerificationDomainService.verify(EMAIL, SIGN_UP, VERIFICATION_CODE)).willThrow(exception);
        EmailVerificationCodeVerifyCommand command = CreateVerifyCommand();

        assertThatThrownBy(() -> emailVerificationService.verify(command)).isSameAs(exception);

        then(emailVerificationDomainService).should().verify(EMAIL, SIGN_UP, VERIFICATION_CODE);
        then(emailVerificationStore).should(never()).save(any(), any());
    }

    @Test
    void 인증_완료_여부를_확인한다() {
        emailVerificationService.checkVerified(createCheckCommand());

        then(emailVerificationDomainService).should().checkVerified(EMAIL, SIGN_UP);
    }

    private EmailVerificationIssueCommand createIssueCommand() {
        return new EmailVerificationIssueCommand(EMAIL, SIGN_UP);
    }

    private EmailVerificationCodeVerifyCommand CreateVerifyCommand() {
        return new EmailVerificationCodeVerifyCommand(EMAIL, SIGN_UP, VERIFICATION_CODE);
    }

    private EmailVerifiedCheckCommand createCheckCommand() {
        return new EmailVerifiedCheckCommand(EMAIL, SIGN_UP);
    }
}
