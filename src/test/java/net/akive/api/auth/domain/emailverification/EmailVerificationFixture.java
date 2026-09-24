package net.akive.api.auth.domain.emailverification;

import java.time.Instant;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;

public class EmailVerificationFixture {

    private EmailVerificationFixture() {
    }

    public static final EmailVerificationCode VERIFICATION_CODE = new EmailVerificationCode("123456");
    public static final Instant REQUESTED_AT = Instant.parse("2025-05-21T01:00:00Z");

    public static EmailVerification createEmailVerification() {
        return createEmailVerification(VERIFICATION_CODE, SIGN_UP, REQUESTED_AT);
    }

    public static EmailVerification createEmailVerification(
            EmailVerificationCode verificationCode,
            Instant requestedAt
    ) {
        return createEmailVerification(verificationCode, SIGN_UP, requestedAt);
    }

    public static EmailVerification createEmailVerification(
            EmailVerificationCode verificationCode,
            EmailVerificationPurpose verificationPurpose,
            Instant requestedAt
    ) {
        return EmailVerification.create(EMAIL, verificationPurpose, verificationCode, requestedAt);
    }

    public static EmailVerification createEmailVerification(EmailVerificationPurpose purpose) {
        return createEmailVerification(VERIFICATION_CODE, purpose, REQUESTED_AT);
    }
}
