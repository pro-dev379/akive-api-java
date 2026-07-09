package net.akive.api.auth.application.dto.result.emailverification;

import net.akive.api.common.domain.Email;

import java.time.LocalDateTime;

public record SendEmailVerificationCodeResult(
        Email email,
        LocalDateTime expiresAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static SendEmailVerificationCodeResult of(
            Email email,
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

    public static SendEmailVerificationCodeResult of(
            String email,
            LocalDateTime expiresAt,
            long expiresInSeconds,
            long resendAfterSeconds
    ) {
        return new SendEmailVerificationCodeResult(
                Email.of(email),
                expiresAt,
                expiresInSeconds,
                resendAfterSeconds
        );
    }
}
