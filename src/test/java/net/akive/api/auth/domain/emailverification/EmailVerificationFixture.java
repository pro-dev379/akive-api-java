package net.akive.api.auth.domain.emailverification;

import net.akive.api.common.domain.Email;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;

public class EmailVerificationFixture {

    public static final String EMAIL_ADDRESS = "test@test.com";
    public static final Email EMAIL = new Email(EMAIL_ADDRESS);
    public static final EmailVerificationCode VERIFICATION_CODE = new EmailVerificationCode("123456");
    public static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2025, 5, 21, 10, 0);

    public static EmailVerification createEmailVerification() {
        return createEmailVerification(SIGN_UP, REQUESTED_AT);
    }

    public static EmailVerification createEmailVerification(
            EmailVerificationPurpose verificationPurpose,
            LocalDateTime requestedAt
    ) {
        return EmailVerification.create(EMAIL, verificationPurpose, VERIFICATION_CODE, requestedAt);
    }

    public static EmailVerification createEmailVerification(EmailVerificationPurpose purpose) {
        return createEmailVerification(purpose, REQUESTED_AT);
    }
}
