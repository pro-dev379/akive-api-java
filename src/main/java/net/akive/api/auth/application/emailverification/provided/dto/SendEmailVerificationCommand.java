package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

public record SendEmailVerificationCommand(
        Email email,
        EmailVerificationPurpose verificationPurpose
) {

    public static SendEmailVerificationCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return new SendEmailVerificationCommand(new Email(email), verificationPurpose);
    }
}
