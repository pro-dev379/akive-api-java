package net.akive.api.auth.presentation.dto.request.emailverification;

import jakarta.validation.constraints.*;
import net.akive.api.auth.application.dto.command.emailverification.VerifyEmailVerificationCodeCommand;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

public record VerifyEmailVerificationCodeRequest(
    @Email @NotBlank String email,
    @NotNull EmailVerificationPurpose verificationPurpose,
    @NotBlank @Pattern(regexp = "\\d{6}") String verificationCode
) {

    public VerifyEmailVerificationCodeCommand to() {
        return VerifyEmailVerificationCodeCommand.of(email, verificationPurpose, verificationCode);
    }
}
