package net.akive.api.auth.presentation.dto.request;

import jakarta.validation.constraints.*;
import net.akive.api.auth.domain.EmailVerificationPurpose;

public record EmailVerificationCodeVerifyRequest(
    @Email @NotBlank  String email,
    @NotNull EmailVerificationPurpose verificationPurpose,
    @NotBlank @Pattern(regexp = "\\d{6}") String verificationCode
) {
}
