package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.common.domain.Email;

import java.time.LocalDateTime;

public record SendEmailVerificationResult(
        Email email,
        LocalDateTime expiresAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static SendEmailVerificationResult of(
            String email,
            LocalDateTime expiresAt,
            long expiresInSeconds,
            long resendAfterSeconds
    ) {
        return new SendEmailVerificationResult(
                new Email(email),
                expiresAt,
                expiresInSeconds,
                resendAfterSeconds
        );
    }
}
