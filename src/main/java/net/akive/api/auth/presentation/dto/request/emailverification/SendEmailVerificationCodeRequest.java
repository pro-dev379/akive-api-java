package net.akive.api.auth.presentation.dto.request.emailverification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCommand;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record SendEmailVerificationCodeRequest(
        @Email @NotBlank String email,
    @NotNull EmailVerificationPurpose verificationPurpose
) {
    public SendEmailVerificationCommand to() {
        return SendEmailVerificationCommand.of(email, verificationPurpose);
    }
}
