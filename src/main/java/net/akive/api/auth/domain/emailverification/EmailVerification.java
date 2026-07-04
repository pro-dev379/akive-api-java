package net.akive.api.auth.domain.emailverification;

import lombok.*;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.*;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.*;

@Getter
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class EmailVerification {

    private String email;
    private EmailVerificationPurpose verificationPurpose;
    private String verificationCode;
    private int resendCount;
    private boolean verified;
    private LocalDateTime expiresAt;
    private LocalDateTime lastSentAt;
    private LocalDateTime createdAt;

    public static EmailVerification create(
            String email,
            EmailVerificationPurpose verificationPurpose,
            String verificationCode,
            LocalDateTime requestedAt
    ) {
        return new EmailVerification(
                email,
                verificationPurpose,
                verificationCode,
                0,
                false,
                requestedAt.plus(EXPIRE_DURATION),
                requestedAt,
                requestedAt
        );
    }

    public void renew(String verificationCode, LocalDateTime requestedAt) {
        validateResendAvailable(requestedAt);

        this.verificationCode = verificationCode;
        this.resendCount++;
        this.verified = false;
        this.expiresAt = requestedAt.plus(EXPIRE_DURATION);
        this.lastSentAt = requestedAt;
    }

    public void verify(String inputVerificationCode, LocalDateTime requestedAt) {
        if (this.verified) {
            throw new EmailVerificationException(ALREADY_VERIFIED);
        }

        if (this.expiresAt.isBefore(requestedAt)) {
            throw new EmailVerificationException(EXPIRED);
        }

        if (!Objects.equals(this.verificationCode, inputVerificationCode)) {
            throw new EmailVerificationException(CODE_MISMATCH);
        }

        this.verified = true;
    }

    private void validateResendAvailable(LocalDateTime requestedAt) {
        if (MAX_RESEND_COUNT <= this.resendCount) {
            throw new EmailVerificationException(RESEND_LIMIT_EXCEEDED);
        }

        if (Duration.between(this.lastSentAt, requestedAt).compareTo(RESEND_INTERVAL) < 0) {
            throw new EmailVerificationException(RESEND_TOO_SOON);
        }
    }
}
