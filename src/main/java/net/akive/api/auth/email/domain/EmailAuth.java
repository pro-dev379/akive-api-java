package net.akive.api.auth.email.domain;

import lombok.*;
import net.akive.api.auth.email.domain.exception.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static net.akive.api.auth.email.domain.EmailAuthPolicy.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailAuth {

    private String email;
    private EmailAuthPurpose authPurpose;
    private String authCode;
    private int resendCount;
    private boolean verified;
    private LocalDateTime expiredAt;
    private LocalDateTime lastSentAt;
    private LocalDateTime createdAt;

    public EmailAuth(String email, EmailAuthPurpose authPurpose, String authCode, LocalDateTime requestedAt) {
        this.email = email;
        this.authPurpose = authPurpose;
        this.authCode = authCode;
        this.resendCount = 0;
        this.verified = false;
        this.expiredAt = requestedAt.plus(EXPIRE_DURATION);
        this.lastSentAt = requestedAt;
        this.createdAt = requestedAt;
    }

    public static EmailAuth create(String email, EmailAuthPurpose authPurpose, String authCode, LocalDateTime requestedAt) {
        return new EmailAuth(email, authPurpose, authCode, requestedAt);
    }

    public void renew(String authCode, LocalDateTime requestedAt) {
        validateResendAvailable(requestedAt);

        this.authCode = authCode;
        this.resendCount++;
        this.verified = false;
        this.expiredAt = requestedAt.plus(EXPIRE_DURATION);
        this.lastSentAt = requestedAt;
    }

    public void verify(String inputAuthCode, LocalDateTime requestedAt) {
        if (this.verified) {
            throw new EmailAuthAlreadyVerifiedException();
        }

        if (this.expiredAt.isBefore(requestedAt)) {
            throw new EmailAuthExpiredException();
        }

        if (!Objects.equals(this.authCode, inputAuthCode)) {
            throw new EmailAuthCodeMismatchException();
        }

        this.verified = true;
    }

    private void validateResendAvailable(LocalDateTime requestedAt) {
        if (MAX_RESEND_COUNT <= this.resendCount) {
            throw new EmailAuthResendLimitExceededException();
        }

        if (Duration.between(this.lastSentAt, requestedAt).compareTo(RESEND_INTERVAL) < 0) {
            throw new EmailAuthResendTooSoonException();
        }
    }
}
