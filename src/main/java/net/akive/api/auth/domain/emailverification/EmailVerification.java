package net.akive.api.auth.domain.emailverification;

import lombok.*;
import net.akive.api.common.domain.Email;
import net.akive.api.auth.exception.EmailVerificationException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.*;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.auth.exception.EmailVerificationErrorCode.*;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class EmailVerification {

    private Email email;
    private EmailVerificationPurpose verificationPurpose;
    private EmailVerificationCode verificationCode;
    private int resendCount;
    private boolean verified;
    private LocalDateTime expiresAt;
    private LocalDateTime lastSentAt;
    private LocalDateTime createdAt;

    public static EmailVerification create(
            Email email,
            EmailVerificationPurpose verificationPurpose,
            EmailVerificationCode verificationCode,
            LocalDateTime requestedAt
    ) {
        LocalDateTime validatedRequestedAt = requireNonNull(requestedAt);

        return EmailVerification.builder()
                .email(requireNonNull(email))
                .verificationPurpose(requireNonNull(verificationPurpose))
                .verificationCode(requireNonNull(verificationCode))
                .resendCount(0)
                .verified(false)
                .expiresAt(validatedRequestedAt.plus(EXPIRE_DURATION))
                .lastSentAt(validatedRequestedAt)
                .createdAt(validatedRequestedAt)
                .build();
    }

    public void renew(EmailVerificationCode verificationCode, LocalDateTime requestedAt) {
        requireNonNull(requestedAt);
        validateResendAvailable(requestedAt);

        this.verificationCode = requireNonNull(verificationCode);
        this.resendCount++;
        this.verified = false;
        this.expiresAt = requestedAt.plus(EXPIRE_DURATION);
        this.lastSentAt = requestedAt;
    }

    public void verify(EmailVerificationCode inputVerificationCode, LocalDateTime requestedAt) {
        EmailVerificationCode validatedInputVerificationCode = requireNonNull(inputVerificationCode);
        requireNonNull(requestedAt);

        if (this.verified) {
            throw new EmailVerificationException(ALREADY_VERIFIED);
        }

        if (this.expiresAt.isBefore(requestedAt)) {
            throw new EmailVerificationException(EXPIRED);
        }

        if (!Objects.equals(this.verificationCode, validatedInputVerificationCode)) {
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
