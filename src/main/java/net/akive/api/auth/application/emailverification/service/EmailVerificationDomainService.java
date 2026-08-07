package net.akive.api.auth.application.emailverification.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.required.GenerateEmailVerificationCodePort;
import net.akive.api.auth.application.emailverification.required.LoadEmailVerificationPort;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.common.domain.Email;
import net.akive.api.common.time.CurrentTimeProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static net.akive.api.common.exception.business.NotFoundError.EMAIL_VERIFICATION_NOT_FOUND;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_NOT_VERIFIED;

@Component
@RequiredArgsConstructor
class EmailVerificationDomainService {

    private final EmailVerificationRequestValidator emailVerificationRequestValidator;
    private final GenerateEmailVerificationCodePort generateEmailVerificationCodePort;
    private final LoadEmailVerificationPort loadEmailVerificationPort;
    private final CurrentTimeProvider currentTimeProvider;

    EmailVerification issue(Email email, EmailVerificationPurpose verificationPurpose) {
        emailVerificationRequestValidator.validateRequestAvailable(email, verificationPurpose);

        LocalDateTime requestedAt = currentTimeProvider.now();
        EmailVerificationCode verificationCode = generateEmailVerificationCodePort.generate();

        return loadEmailVerificationPort
                .loadByEmailAndVerificationPurpose(email, verificationPurpose)
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
        EmailVerification emailVerification = loadEmailVerificationPort
                .loadByEmailAndVerificationPurpose(email, verificationPurpose)
                .orElseThrow(() -> new NotFoundException(EMAIL_VERIFICATION_NOT_FOUND));

        emailVerification.verify(verificationCode, currentTimeProvider.now());

        return emailVerification;
    }

    void ensureVerified(Email email, EmailVerificationPurpose verificationPurpose) {
        EmailVerification emailVerification = loadEmailVerificationPort
                .loadByEmailAndVerificationPurpose(email, verificationPurpose)
                .orElseThrow(() -> new BadRequestException(EMAIL_NOT_VERIFIED));

        if (!emailVerification.isVerified()) {
            throw new BadRequestException(EMAIL_NOT_VERIFIED);
        }
    }
}
