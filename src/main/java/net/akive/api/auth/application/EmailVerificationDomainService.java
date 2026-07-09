package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.emailverification.GenerateEmailVerificationCodePort;
import net.akive.api.auth.application.port.out.emailverification.LoadEmailVerificationPort;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.common.domain.Email;
import net.akive.api.common.time.CurrentTimeProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.NOT_FOUND;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.NOT_VERIFIED;

@Component
@RequiredArgsConstructor
class EmailVerificationDomainService {

    private final EmailVerificationRequestPolicy emailVerificationRequestPolicy;
    private final GenerateEmailVerificationCodePort generateEmailVerificationCodePort;
    private final LoadEmailVerificationPort loadEmailVerificationPort;
    private final CurrentTimeProvider currentTimeProvider;

    EmailVerification issue(Email email, EmailVerificationPurpose verificationPurpose) {
        emailVerificationRequestPolicy.validateRequestAvailable(email, verificationPurpose);

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
                .orElseThrow(() -> new EmailVerificationException(NOT_FOUND));

        emailVerification.verify(verificationCode, currentTimeProvider.now());

        return emailVerification;
    }

    void ensureVerified(Email email, EmailVerificationPurpose verificationPurpose) {
        EmailVerification emailVerification = loadEmailVerificationPort
                .loadByEmailAndVerificationPurpose(email, verificationPurpose)
                .orElseThrow(() -> new EmailVerificationException(NOT_VERIFIED));

        if (!emailVerification.isVerified()) {
            throw new EmailVerificationException(NOT_VERIFIED);
        }
    }
}
