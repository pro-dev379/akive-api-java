package net.akive.api.auth.adapter.emailverification.webapi.dto.response;

import net.akive.api.auth.application.emailverification.provided.dto.SendEmailVerificationResult;

import java.time.LocalDateTime;

public record SendEmailVerificationCodeResponse(
        String email,
        LocalDateTime expiresAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static SendEmailVerificationCodeResponse from(SendEmailVerificationResult result) {
        return new SendEmailVerificationCodeResponse(
                result.email().value(),
                result.expiresAt(),
                result.expireSeconds(),
                result.resendIntervalSeconds()
        );
    }
}
