package net.akive.api.auth.email.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailAuthCodeSendResponse(
        String email,
        LocalDateTime expiredAt,
        int expiresInSeconds,
        int resendAfterSeconds
) {

    public static EmailAuthCodeSendResponse of(
            String email,
            LocalDateTime expiredAt,
            int expiresInSeconds,
            int resendAfterSeconds
    ) {
        return new EmailAuthCodeSendResponse(
                email,
                expiredAt,
                expiresInSeconds,
                resendAfterSeconds
        );
    }
}
