package net.akive.api.auth.presentation.dto.request.emailverification;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeSendCommand;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record EmailVerificationCodeSendRequest(
        @Email @NotBlank String email,
        @NotNull EmailVerificationPurpose verificationPurpose
) {
    public EmailVerificationCodeSendCommand toCommand() {
        return new EmailVerificationCodeSendCommand(email, verificationPurpose);
    }
}
