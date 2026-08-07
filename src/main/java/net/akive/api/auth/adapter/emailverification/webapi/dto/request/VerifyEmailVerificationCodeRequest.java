package net.akive.api.auth.adapter.emailverification.webapi.dto.request;

import jakarta.validation.constraints.*;
import net.akive.api.auth.application.emailverification.provided.dto.VerifyEmailVerificationCodeCommand;
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
