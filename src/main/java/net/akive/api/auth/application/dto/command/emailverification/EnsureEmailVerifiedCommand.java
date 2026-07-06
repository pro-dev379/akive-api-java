package net.akive.api.auth.application.dto.command.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record EnsureEmailVerifiedCommand(
        String email,
        EmailVerificationPurpose verificationPurpose
) {

    public static EnsureEmailVerifiedCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return new EnsureEmailVerifiedCommand(email, verificationPurpose);
    }
}
