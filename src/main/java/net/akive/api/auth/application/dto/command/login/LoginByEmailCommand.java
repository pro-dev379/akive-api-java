package net.akive.api.auth.application.dto.command.login;

import net.akive.api.common.domain.Email;

public record LoginByEmailCommand(Email email, String password) {

    public static LoginByEmailCommand of(String email, String password) {
        return new LoginByEmailCommand(Email.of(email), password);
    }
}
