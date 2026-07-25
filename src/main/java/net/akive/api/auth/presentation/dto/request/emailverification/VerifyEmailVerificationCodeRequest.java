package net.akive.api.auth.presentation.dto.request.emailverification;

import jakarta.validation.constraints.*;
import net.akive.api.auth.application.dto.command.emailverification.VerifyEmailVerificationCodeCommand;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_REGEX;

public record VerifyEmailVerificationCodeRequest(
    @Email @NotBlank String email,
    @NotNull EmailVerificationPurpose verificationPurpose,
    @NotBlank @Pattern(regexp = VERIFICATION_CODE_REGEX) String verificationCode
) {

    public VerifyEmailVerificationCodeCommand to() {
        return VerifyEmailVerificationCodeCommand.of(email, verificationPurpose, verificationCode);
    }
}
