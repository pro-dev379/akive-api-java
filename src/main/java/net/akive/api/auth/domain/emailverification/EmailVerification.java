package net.akive.api.auth.domain.emailverification;

import lombok.*;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.TooManyRequestsException;
import net.akive.api.common.exception.business.UnauthorizedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.*;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.common.exception.business.BadRequestError.*;
import static net.akive.api.common.exception.business.ConflictError.*;
import static net.akive.api.common.exception.business.TooManyRequestsError.*;
import static net.akive.api.common.exception.business.UnauthorizedError.*;

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
                .expiresAt(validatedRequestedAt.plus(VERIFICATION_CODE_EXPIRE_DURATION))
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
        this.expiresAt = requestedAt.plus(VERIFICATION_CODE_EXPIRE_DURATION);
        this.lastSentAt = requestedAt;
    }

    public void verify(EmailVerificationCode inputVerificationCode, LocalDateTime requestedAt) {
        EmailVerificationCode validatedInputVerificationCode = requireNonNull(inputVerificationCode);
        requireNonNull(requestedAt);

        if (this.verified) {
            throw new ConflictException(EMAIL_VERIFICATION_ALREADY_VERIFIED);
        }

        if (this.expiresAt.isBefore(requestedAt)) {
            throw new UnauthorizedException(EXPIRED_EMAIL_VERIFICATION_CODE);
        }

        if (!Objects.equals(this.verificationCode, validatedInputVerificationCode)) {
            throw new BadRequestException(EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        this.verified = true;
    }

    private void validateResendAvailable(LocalDateTime requestedAt) {
        if (this.resendCount >= EMAIL_VERIFICATION_RESEND_MAX_COUNT) {
            throw new TooManyRequestsException(EMAIL_VERIFICATION_REQUEST_LIMIT_EXCEEDED);
        }

        if (Duration.between(this.lastSentAt, requestedAt).compareTo(EMAIL_VERIFICATION_RESEND_MIN_INTERVAL) < 0) {
            throw new TooManyRequestsException(EMAIL_VERIFICATION_REQUEST_TOO_FREQUENT);
        }
    }
}
