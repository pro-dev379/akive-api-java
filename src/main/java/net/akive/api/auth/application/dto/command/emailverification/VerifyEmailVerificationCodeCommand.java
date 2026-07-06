package net.akive.api.auth.application.dto.command.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record VerifyEmailVerificationCodeCommand(
        String email,
        EmailVerificationPurpose verificationPurpose,
        String verificationCode
) {

    public static VerifyEmailVerificationCodeCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose,
            String verificationCode
    ) {
        return new VerifyEmailVerificationCodeCommand(email, verificationPurpose, verificationCode);
    }
}
