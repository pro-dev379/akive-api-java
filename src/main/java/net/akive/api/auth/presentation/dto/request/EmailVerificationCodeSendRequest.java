package net.akive.api.auth.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.auth.domain.EmailVerificationPurpose;

public record EmailVerificationCodeSendRequest(
        @Email @NotBlank String email,
        @NotNull EmailVerificationPurpose verificationPurpose
) {}
