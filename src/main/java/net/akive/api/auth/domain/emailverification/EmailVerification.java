package net.akive.api.auth.domain.emailverification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.TooManyRequestsException;
import net.akive.api.common.exception.business.UnauthorizedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_VERIFICATION_CODE_MISMATCH;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_VERIFIED;
import static net.akive.api.common.exception.business.TooManyRequestsError.EMAIL_VERIFICATION_RENEWAL_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.TooManyRequestsError.EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT;
import static net.akive.api.common.exception.business.UnauthorizedError.EMAIL_VERIFICATION_CODE_EXPIRED;

@Getter
@Builder(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class EmailVerification {

    private Email email;

    private EmailVerificationPurpose verificationPurpose;

    private EmailVerificationCode verificationCode;

    private int renewalCount;

    @JsonProperty("verified")
    private boolean isVerified;

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
                .renewalCount(0)
                .isVerified(false)
                .expiresAt(validatedRequestedAt.plus(VERIFICATION_CODE_EXPIRATION_DURATION))
                .lastSentAt(validatedRequestedAt)
                .createdAt(validatedRequestedAt)
                .build();
    }

    public void renew(EmailVerificationCode verificationCode, LocalDateTime requestedAt) {
        requireNonNull(requestedAt);
        validateRenewable(requestedAt);

        this.verificationCode = requireNonNull(verificationCode);
        this.renewalCount++;
        this.isVerified = false;
        this.expiresAt = requestedAt.plus(VERIFICATION_CODE_EXPIRATION_DURATION);
        this.lastSentAt = requestedAt;
    }

    public void verify(EmailVerificationCode inputVerificationCode, LocalDateTime requestedAt) {
        EmailVerificationCode validatedInputVerificationCode = requireNonNull(inputVerificationCode);
        requireNonNull(requestedAt);

        if (this.isVerified) {
            throw new ConflictException(EMAIL_ALREADY_VERIFIED);
        }

        if (this.expiresAt.isBefore(requestedAt)) {
            throw new UnauthorizedException(EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!Objects.equals(this.verificationCode, validatedInputVerificationCode)) {
            throw new BadRequestException(EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        this.isVerified = true;
    }

    private void validateRenewable(LocalDateTime requestedAt) {
        if (this.renewalCount >= EMAIL_VERIFICATION_RENEWAL_MAX_COUNT) {
            throw new TooManyRequestsException(EMAIL_VERIFICATION_RENEWAL_LIMIT_EXCEEDED);
        }

        if (isRenewalRequestedTooFrequently(requestedAt)) {
            throw new TooManyRequestsException(EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT);
        }
    }

    private boolean isRenewalRequestedTooFrequently(LocalDateTime requestedAt) {
        return Duration.between(this.lastSentAt, requestedAt)
                .compareTo(EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL) < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmailVerification that = (EmailVerification) o;

        return Objects.equals(this.email, that.email)
                && verificationPurpose == that.verificationPurpose;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.email, this.verificationPurpose);
    }
}
