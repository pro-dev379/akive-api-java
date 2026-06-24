package net.akive.api.auth.application.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record EmailVerificationCodeSendCommand(
        String email,
        EmailVerificationPurpose verificationPurpose
) {

    public static EmailVerificationCodeSendCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return new EmailVerificationCodeSendCommand(email, verificationPurpose);
    }
}
