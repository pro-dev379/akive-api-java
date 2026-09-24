package net.akive.api.auth.domain.emailverification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.RateLimitExceededException;
import net.akive.api.common.exception.business.ResourceConflictException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.*;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.exception.business.AuthenticationError.EMAIL_VERIFICATION_CODE_EXPIRED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_VERIFICATION_CODE_MISMATCH;
import static net.akive.api.common.exception.business.RateLimitError.EMAIL_VERIFICATION_RENEWAL_LIMIT_EXCEEDED;
import static net.akive.api.common.exception.business.RateLimitError.EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT;
import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_VERIFIED;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class EmailVerification {

    private Email email;

    private EmailVerificationPurpose verificationPurpose;

    private EmailVerificationCode verificationCode;

    private int renewalCount;

    private boolean verified;

    private Instant expiresAt;

    private Instant lastSentAt;

    private Instant createdAt;

    public static EmailVerification create(
            Email email,
            EmailVerificationPurpose verificationPurpose,
            EmailVerificationCode verificationCode,
            Instant requestedAt
    ) {
        Instant validatedRequestedAt = requireNonNull(requestedAt);

        return EmailVerification.builder()
                .email(requireNonNull(email))
                .verificationPurpose(requireNonNull(verificationPurpose))
                .verificationCode(requireNonNull(verificationCode))
                .renewalCount(0)
                .verified(false)
                .expiresAt(validatedRequestedAt.plus(VERIFICATION_CODE_EXPIRATION_DURATION))
                .lastSentAt(validatedRequestedAt)
                .createdAt(validatedRequestedAt)
                .build();
    }

    public void renew(EmailVerificationCode verificationCode, Instant requestedAt) {
        requireNonNull(requestedAt);
        validateRenewable(requestedAt);

        this.verificationCode = requireNonNull(verificationCode);
        this.renewalCount++;
        this.verified = false;
        this.expiresAt = requestedAt.plus(VERIFICATION_CODE_EXPIRATION_DURATION);
        this.lastSentAt = requestedAt;
    }

    public void verify(EmailVerificationCode inputVerificationCode, Instant requestedAt) {
        EmailVerificationCode validatedInputVerificationCode = requireNonNull(inputVerificationCode);
        requireNonNull(requestedAt);

        if (this.verified) {
            throw new ResourceConflictException(EMAIL_ALREADY_VERIFIED);
        }

        if (this.expiresAt.isBefore(requestedAt)) {
            throw new AuthenticationException(EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!Objects.equals(this.verificationCode, validatedInputVerificationCode)) {
            throw new BusinessRuleViolationException(EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        this.verified = true;
    }

    private void validateRenewable(Instant requestedAt) {
        if (this.renewalCount >= EMAIL_VERIFICATION_RENEWAL_MAX_COUNT) {
            throw new RateLimitExceededException(EMAIL_VERIFICATION_RENEWAL_LIMIT_EXCEEDED);
        }

        if (isRenewalRequestedTooFrequently(requestedAt)) {
            throw new RateLimitExceededException(EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT);
        }
    }

    private boolean isRenewalRequestedTooFrequently(Instant requestedAt) {
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
