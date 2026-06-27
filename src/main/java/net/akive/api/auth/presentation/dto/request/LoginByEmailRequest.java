package net.akive.api.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import net.akive.api.auth.application.dto.command.login.LoginByEmailCommand;

public record LoginByEmailRequest(
        @NotBlank String email,
        @NotBlank String password
) {

    public LoginByEmailCommand toCommand() {
        return new LoginByEmailCommand(email, password);
    }
}
