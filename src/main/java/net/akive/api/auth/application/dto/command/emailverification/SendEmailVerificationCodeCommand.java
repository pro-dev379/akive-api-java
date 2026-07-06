package net.akive.api.auth.application.dto.command.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record SendEmailVerificationCodeCommand(
        String email,
        EmailVerificationPurpose verificationPurpose
) {

    public static SendEmailVerificationCodeCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return new SendEmailVerificationCodeCommand(email, verificationPurpose);
    }
}
