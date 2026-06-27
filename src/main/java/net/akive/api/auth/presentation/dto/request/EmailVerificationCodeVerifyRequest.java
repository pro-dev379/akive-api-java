package net.akive.api.auth.presentation.dto.request;

import jakarta.validation.constraints.*;
import net.akive.api.auth.application.dto.command.emailverification.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record EmailVerificationCodeVerifyRequest(
    @Email @NotBlank  String email,
    @NotNull EmailVerificationPurpose verificationPurpose,
    @NotBlank @Pattern(regexp = "\\d{6}") String verificationCode
) {

    public EmailVerificationCodeVerifyCommand toCommand() {
        return new EmailVerificationCodeVerifyCommand(email, verificationPurpose, verificationCode);
    }
}
