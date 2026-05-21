package net.akive.api.auth.email.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.akive.api.auth.email.domain.EmailAuthPurpose;

public record EmailAuthCodeSendRequest(
        @Email @NotBlank String email,
        @NotNull EmailAuthPurpose authPurpose
) {}
