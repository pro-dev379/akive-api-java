package net.akive.api.auth.application.dto.command.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record EmailVerificationCodeVerifyCommand(
        String email,
        EmailVerificationPurpose verificationPurpose,
        String verificationCode
) {

    public static EmailVerificationCodeVerifyCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose,
            String verificationCode
    ) {
        return new EmailVerificationCodeVerifyCommand(email, verificationPurpose, verificationCode);
    }
}
