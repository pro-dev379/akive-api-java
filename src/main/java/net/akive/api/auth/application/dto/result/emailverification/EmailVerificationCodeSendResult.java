package net.akive.api.auth.application.dto.result.emailverification;

import java.time.LocalDateTime;

public record EmailVerificationCodeSendResult(
        String email,
        LocalDateTime expiresAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static EmailVerificationCodeSendResult of(
            String email,
            LocalDateTime expiresAt,
            long expiresInSeconds,
            long resendAfterSeconds
    ) {
        return new EmailVerificationCodeSendResult(
                email,
                expiresAt,
                expiresInSeconds,
                resendAfterSeconds
        );
    }
}
