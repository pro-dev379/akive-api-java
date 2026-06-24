package net.akive.api.auth.application.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record EmailVerificationEnsureVerifiedCommand(
        String email,
        EmailVerificationPurpose verificationPurpose
) {

    public static EmailVerificationEnsureVerifiedCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return new EmailVerificationEnsureVerifiedCommand(email, verificationPurpose);
    }
}
