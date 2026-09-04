package net.akive.api.auth.application.emailverification.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.required.EmailVerificationCodeGenerator;
import net.akive.api.auth.application.emailverification.required.EmailVerificationStore;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.common.time.CurrentTimeProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.EMAIL_VERIFICATION_NOT_FOUND;

@Component
@RequiredArgsConstructor
class EmailVerificationDomainService {

    private final EmailVerificationCodeGenerator emailVerificationCodeGenerator;
    private final EmailVerificationStore emailVerificationStore;

    private final EmailVerificationValidationService emailVerificationValidationService;
    private final CurrentTimeProvider currentTimeProvider;

    EmailVerification issue(Email email, EmailVerificationPurpose verificationPurpose) {
        emailVerificationValidationService.validateIssuable(email, verificationPurpose);

        LocalDateTime requestedAt = currentTimeProvider.now();
        EmailVerificationCode verificationCode = emailVerificationCodeGenerator.generate();

        return emailVerificationStore
                .findByEmailAndVerificationPurpose(email, verificationPurpose)
                .map(previous -> {
                    previous.renew(verificationCode, requestedAt);

                    return previous;
                })
                .orElseGet(() -> EmailVerification.create(email, verificationPurpose, verificationCode, requestedAt));
    }

    EmailVerification verify(
            Email email,
            EmailVerificationPurpose verificationPurpose,
            EmailVerificationCode verificationCode
    ) {
        EmailVerification emailVerification = emailVerificationStore
                .findByEmailAndVerificationPurpose(email, verificationPurpose)
                .orElseThrow(() -> new ResourceNotFoundException(EMAIL_VERIFICATION_NOT_FOUND));

        emailVerification.verify(verificationCode, currentTimeProvider.now());

        return emailVerification;
    }

    void checkVerified(Email email, EmailVerificationPurpose verificationPurpose) {
        EmailVerification emailVerification = emailVerificationStore
                .findByEmailAndVerificationPurpose(email, verificationPurpose)
                .orElseThrow(() -> new BusinessRuleViolationException(EMAIL_NOT_VERIFIED));

        if (!emailVerification.isVerified()) {
            throw new BusinessRuleViolationException(EMAIL_NOT_VERIFIED);
        }
    }
}
