package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record EmailVerificationIssueCommand(Email email, EmailVerificationPurpose verificationPurpose) {

    public EmailVerificationIssueCommand {
        requireNonNull(email);
        requireNonNull(verificationPurpose);
    }

    public static EmailVerificationIssueCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return new EmailVerificationIssueCommand(new Email(email), verificationPurpose);
    }
}
