package net.akive.api.auth.email.presentation.dto.request;

import jakarta.validation.constraints.*;
import net.akive.api.auth.email.domain.EmailVerificationPurpose;

public record EmailVerificationCodeVerifyRequest(
    @Email @NotBlank  String email,
    @NotNull EmailVerificationPurpose verificationPurpose,
    @NotBlank @Pattern(regexp = "\\d{6}") String verificationCode
) {
}
