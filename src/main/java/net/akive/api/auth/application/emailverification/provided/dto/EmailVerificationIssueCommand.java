package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

public record EmailVerificationIssueCommand(Email email, EmailVerificationPurpose verificationPurpose) {

    public static EmailVerificationIssueCommand of(String email, EmailVerificationPurpose verificationPurpose) {
        return new EmailVerificationIssueCommand(new Email(email), verificationPurpose);
    }
}
