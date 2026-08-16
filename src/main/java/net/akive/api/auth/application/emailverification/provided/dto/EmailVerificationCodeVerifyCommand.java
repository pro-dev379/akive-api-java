package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record EmailVerificationCodeVerifyCommand(
        Email email,
        EmailVerificationPurpose verificationPurpose,
        EmailVerificationCode verificationCode
) {

    public EmailVerificationCodeVerifyCommand {
        requireNonNull(email);
        requireNonNull(verificationPurpose);
        requireNonNull(verificationCode);
    }

    public static EmailVerificationCodeVerifyCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose,
            String verificationCode
    ) {
        return new EmailVerificationCodeVerifyCommand(
                new Email(email),
                verificationPurpose,
                new EmailVerificationCode(verificationCode)
        );
    }
}
