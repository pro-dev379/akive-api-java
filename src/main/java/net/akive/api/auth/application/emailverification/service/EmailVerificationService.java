package net.akive.api.auth.application.emailverification.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationChecker;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeIssuer;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeVerifier;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationEnsureCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.auth.application.emailverification.required.EmailVerificationSender;
import net.akive.api.auth.application.emailverification.required.EmailVerificationStore;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.exception.business.RateLimitExceededException;
import net.akive.api.common.exception.business.AuthenticationException;
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
        EmailVerificationChecker {

    private final EmailVerificationStore emailVerificationStore;
    private final EmailVerificationSender emailVerificationSender;

    private final EmailVerificationDomainService emailVerificationDomainService;

    @Override
    public EmailVerification issue(EmailVerificationIssueCommand command)
            throws ResourceConflictException, ResourceNotFoundException, RateLimitExceededException {
        EmailVerification emailVerification = emailVerificationDomainService.issue(
                command.email(),
                command.verificationPurpose()
        );

        emailVerificationSender.send(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);
        emailVerificationStore.save(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);

        return emailVerification;
    }

    @Override
    public void verify(EmailVerificationCodeVerifyCommand command)
            throws BusinessRuleViolationException,
            ResourceNotFoundException,
            AuthenticationException,
            ResourceConflictException {
        EmailVerification emailVerification = emailVerificationDomainService.verify(
                command.email(),
                command.verificationPurpose(),
                command.verificationCode()
        );

        emailVerificationStore.save(emailVerification, VERIFIED_EMAIL_VERIFICATION_EXPIRATION_DURATION);
    }

    @Override
    @Transactional(readOnly = true)
    public void ensureEmailVerified(EmailVerificationEnsureCommand command) throws BusinessRuleViolationException {
        emailVerificationDomainService.checkVerified(command.email(), command.verificationPurpose());
    }
}
