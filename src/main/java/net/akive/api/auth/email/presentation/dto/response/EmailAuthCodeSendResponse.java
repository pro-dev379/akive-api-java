package net.akive.api.auth.email.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailAuthCodeSendResponse(
        String email,
        LocalDateTime expiredAt,
        long expireSeconds,
        long resendIntervalSeconds
) {

    public static EmailAuthCodeSendResponse from(EmailAuthCodeSendResult result) {
        return new EmailAuthCodeSendResponse(
                result.email(),
                result.expiredAt(),
                result.expireSeconds(),
                result.resendIntervalSeconds()
        );
    }
}
