package net.akive.api.auth.adapter.emailverification.webapi.dto.response;

import net.akive.api.auth.domain.emailverification.EmailVerification;

import java.time.Instant;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;

public record EmailVerificationCodeIssueResponse(
        String email,
        Instant expiresAt,
        long expirationSeconds,
        long renewalIntervalSeconds
) {

    public static EmailVerificationCodeIssueResponse fromResult(EmailVerification emailVerification) {
        return new EmailVerificationCodeIssueResponse(
                emailVerification.getEmail().value(),
                emailVerification.getExpiresAt(),
                VERIFICATION_CODE_EXPIRATION_DURATION.toSeconds(),
                EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL.toSeconds()
        );
    }
}
