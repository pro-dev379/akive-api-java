package net.akive.api.auth.domain.emailverification;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;

public class EmailVerificationFixture {

    public static final EmailVerificationCode VERIFICATION_CODE = new EmailVerificationCode("123456");
    public static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2025, 5, 21, 10, 0);

    public static EmailVerification createEmailVerification() {
        return createEmailVerification(VERIFICATION_CODE, SIGN_UP, REQUESTED_AT);
    }

    public static EmailVerification createEmailVerification(
            EmailVerificationCode verificationCode,
            LocalDateTime requestedAt
    ) {
        return createEmailVerification(verificationCode, SIGN_UP, requestedAt);
    }

    public static EmailVerification createEmailVerification(
            EmailVerificationCode verificationCode,
            EmailVerificationPurpose verificationPurpose,
            LocalDateTime requestedAt
    ) {
        return EmailVerification.create(EMAIL, verificationPurpose, verificationCode, requestedAt);
    }

    public static EmailVerification createEmailVerification(EmailVerificationPurpose purpose) {
        return createEmailVerification(VERIFICATION_CODE, purpose, REQUESTED_AT);
    }
}
