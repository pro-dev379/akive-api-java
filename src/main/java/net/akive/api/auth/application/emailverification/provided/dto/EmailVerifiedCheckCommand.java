package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record EmailVerifiedCheckCommand(Email email, EmailVerificationPurpose verificationPurpose) {

    public EmailVerifiedCheckCommand {
        requireNonNull(email);
        requireNonNull(verificationPurpose);
    }
}
