package net.akive.api.auth.application.emailverification.provided.dto;

import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

public record VerifyEmailVerificationCodeCommand(
        Email email,
        EmailVerificationPurpose verificationPurpose,
        EmailVerificationCode verificationCode
) {

    public static VerifyEmailVerificationCodeCommand of(
            String email,
            EmailVerificationPurpose verificationPurpose,
            String verificationCode
    ) {
        return new VerifyEmailVerificationCodeCommand(
                new Email(email),
                verificationPurpose,
                new EmailVerificationCode(verificationCode)
        );
    }
}
