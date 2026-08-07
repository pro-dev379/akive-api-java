package net.akive.api.auth.adapter.login.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;

public record LoginByEmailRequest(
        @NotBlank String email,
        @NotBlank String password
) {

    public LoginByEmailCommand to() {
        return LoginByEmailCommand.of(email, password);
    }
}
