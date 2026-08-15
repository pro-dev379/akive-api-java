package net.akive.api.auth.adapter.login.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginByEmailRequest(
        @NotBlank String email,
        @NotBlank String password
) {
}
