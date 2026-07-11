package net.akive.api.auth.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
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
