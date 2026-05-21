package net.akive.api.auth.email.domain;

import lombok.*;
import net.akive.api.auth.email.domain.exception.EmailAuthResendLimitExceededException;
import net.akive.api.auth.email.domain.exception.EmailAuthResendTooSoonException;

import java.time.Duration;
import java.time.LocalDateTime;

import static net.akive.api.auth.email.domain.EmailAuthPolicy.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailAuth {

    private String authCode;
    private int resendCount;
    private boolean verified;
    private LocalDateTime expiredAt;
    private LocalDateTime lastSentAt;
    private LocalDateTime createdAt;

    public static EmailAuth create(String authCode, LocalDateTime requestedAt) {
        return new EmailAuth(
                authCode,
                0,
                false,
                requestedAt.plus(EXPIRE_DURATION),
                requestedAt,
                requestedAt
        );
    }

    public EmailAuth renew(String authCode, LocalDateTime requestedAt) {
        validateResendAvailable(requestedAt);

        return new EmailAuth(
                authCode,
                this.resendCount + 1,
                false,
                requestedAt.plus(EXPIRE_DURATION),
                requestedAt,
                this.createdAt
        );
    }

    private void validateResendAvailable(LocalDateTime requestedAt) {
        if (this.resendCount >= MAX_RESEND_COUNT) {
            throw new EmailAuthResendLimitExceededException();
        }

        if (Duration.between(this.lastSentAt, requestedAt).compareTo(RESEND_INTERVAL) < 0) {
            throw new EmailAuthResendTooSoonException();
        }
    }
}
