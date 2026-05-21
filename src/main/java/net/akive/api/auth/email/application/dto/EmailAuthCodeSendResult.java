package net.akive.api.auth.email.application.dto;

import java.time.LocalDateTime;

public record EmailAuthCodeSendResult(
        String email,
        LocalDateTime expiredAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static EmailAuthCodeSendResult of(
            String email,
            LocalDateTime expiredAt,
            long expiresInSeconds,
            long resendAfterSeconds
    ) {
        return new EmailAuthCodeSendResult(
                email,
                expiredAt,
                expiresInSeconds,
                resendAfterSeconds
        );
    }
}
