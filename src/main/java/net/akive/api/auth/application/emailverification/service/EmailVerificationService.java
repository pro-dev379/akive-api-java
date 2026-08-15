package net.akive.api.auth.application.emailverification.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeIssuer;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeVerifier;
import net.akive.api.auth.application.emailverification.provided.EmailVerifiedChecker;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerifiedCheckCommand;
import net.akive.api.auth.application.emailverification.required.EmailVerificationStore;
import net.akive.api.auth.application.emailverification.required.EmailVerificationSender;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFIED_EMAIL_VERIFICATION_EXPIRATION_DURATION;

@Service
@Transactional
@RequiredArgsConstructor
class EmailVerificationService implements
        EmailVerificationCodeIssuer,
        EmailVerificationCodeVerifier,
        EmailVerifiedChecker {

    private final EmailVerificationStore emailVerificationStore;
    private final EmailVerificationSender emailVerificationSender;

    private final EmailVerificationDomainService emailVerificationDomainService;

    @Override
    public EmailVerification issue(EmailVerificationIssueCommand command) {
        EmailVerification emailVerification = emailVerificationDomainService.issue(
                command.email(),
                command.verificationPurpose()
        );

        emailVerificationSender.send(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);
        emailVerificationStore.save(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);

        return emailVerification;
    }

    @Override
    public void verify(EmailVerificationCodeVerifyCommand command) {
        EmailVerification emailVerification = emailVerificationDomainService.verify(
                command.email(),
                command.verificationPurpose(),
                command.verificationCode()
        );

        emailVerificationStore.save(emailVerification, VERIFIED_EMAIL_VERIFICATION_EXPIRATION_DURATION);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkVerified(EmailVerifiedCheckCommand command) {
        emailVerificationDomainService.checkVerified(command.email(), command.verificationPurpose());
    }
}
