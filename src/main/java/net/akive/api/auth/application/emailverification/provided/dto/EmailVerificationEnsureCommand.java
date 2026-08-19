package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record EmailVerificationEnsureCommand(Email email, EmailVerificationPurpose verificationPurpose) {

    public EmailVerificationEnsureCommand {
        requireNonNull(email);
        requireNonNull(verificationPurpose);
    }
}
