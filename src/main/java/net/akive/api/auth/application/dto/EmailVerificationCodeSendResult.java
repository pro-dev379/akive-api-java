package net.akive.api.auth.application.dto;

import java.time.LocalDateTime;

public record EmailVerificationCodeSendResult(
        String email,
        LocalDateTime expiredAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static EmailVerificationCodeSendResult of(
            String email,
            LocalDateTime expiredAt,
            long expiresInSeconds,
            long resendAfterSeconds
    ) {
        return new EmailVerificationCodeSendResult(
                email,
                expiredAt,
                expiresInSeconds,
                resendAfterSeconds
        );
    }
}
