package net.akive.api.auth.email.presentation.dto.request;

import jakarta.validation.constraints.*;
import net.akive.api.auth.email.domain.EmailAuthPurpose;

public record EmailAuthCodeVerifyRequest(
    @Email @NotBlank  String email,
    @NotNull EmailAuthPurpose authPurpose,
    @NotBlank @Pattern(regexp = "\\d{6}") String authCode
) {
}
