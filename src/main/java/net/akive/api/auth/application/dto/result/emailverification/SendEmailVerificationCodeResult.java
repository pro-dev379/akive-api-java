package net.akive.api.auth.application.dto.result.emailverification;

import java.time.LocalDateTime;

public record SendEmailVerificationCodeResult(
        String email,
        LocalDateTime expiresAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static SendEmailVerificationCodeResult of(
            String email,
            LocalDateTime expiresAt,
            long expiresInSeconds,
            long resendAfterSeconds
    ) {
        return new SendEmailVerificationCodeResult(
                email,
                expiresAt,
                expiresInSeconds,
                resendAfterSeconds
        );
    }
}
