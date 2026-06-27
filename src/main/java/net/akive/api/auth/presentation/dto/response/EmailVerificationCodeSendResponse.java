package net.akive.api.auth.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.auth.application.dto.result.EmailVerificationCodeSendResult;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailVerificationCodeSendResponse(
        String email,
        LocalDateTime expiredAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static EmailVerificationCodeSendResponse from(EmailVerificationCodeSendResult result) {
        return new EmailVerificationCodeSendResponse(
                result.email(),
                result.expiredAt(),
                result.expireSeconds(),
                result.resendIntervalSeconds()
        );
    }
}
