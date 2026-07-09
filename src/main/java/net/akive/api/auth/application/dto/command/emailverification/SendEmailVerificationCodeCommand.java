package net.akive.api.auth.application.dto.command.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

public record SendEmailVerificationCodeCommand(
        Email email,
        EmailVerificationPurpose verificationPurpose
) {

    public static SendEmailVerificationCodeCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return new SendEmailVerificationCodeCommand(Email.of(email), verificationPurpose);
    }
}
