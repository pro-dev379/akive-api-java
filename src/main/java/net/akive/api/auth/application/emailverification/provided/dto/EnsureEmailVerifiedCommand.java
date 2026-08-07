package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

public record EnsureEmailVerifiedCommand(
        Email email,
        EmailVerificationPurpose verificationPurpose
) {
}
