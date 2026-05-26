package net.akive.api.auth.email.domain;

import lombok.*;
import net.akive.api.auth.email.domain.exception.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static net.akive.api.auth.email.domain.EmailVerificationPolicy.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailVerification {

    private String email;
    private EmailVerificationPurpose verificationPurpose;
    private String verificationCode;
    private int resendCount;
    private boolean verified;
    private LocalDateTime expiredAt;
    private LocalDateTime lastSentAt;
    private LocalDateTime createdAt;

    public EmailVerification(
            String email,
            EmailVerificationPurpose verificationPurpose,
            String verificationCode,
            LocalDateTime requestedAt
    ) {
        this.email = email;
        this.verificationPurpose = verificationPurpose;
        this.verificationCode = verificationCode;
        this.resendCount = 0;
        this.verified = false;
        this.expiredAt = requestedAt.plus(EXPIRE_DURATION);
        this.lastSentAt = requestedAt;
        this.createdAt = requestedAt;
    }

    public static EmailVerification create(
            String email,
            EmailVerificationPurpose verificationPurpose,
            String verificationCode,
            LocalDateTime requestedAt
    ) {
        return new EmailVerification(email, verificationPurpose, verificationCode, requestedAt);
    }

    public void renew(String verificationCode, LocalDateTime requestedAt) {
        validateResendAvailable(requestedAt);

        this.verificationCode = verificationCode;
        this.resendCount++;
        this.verified = false;
        this.expiredAt = requestedAt.plus(EXPIRE_DURATION);
        this.lastSentAt = requestedAt;
    }

    public void verify(String inputVerificationCode, LocalDateTime requestedAt) {
        if (this.verified) {
            throw new EmailAuthAlreadyVerifiedException();
        }

        if (this.expiredAt.isBefore(requestedAt)) {
            throw new EmailAuthExpiredException();
        }

        if (!Objects.equals(this.verificationCode, inputVerificationCode)) {
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
