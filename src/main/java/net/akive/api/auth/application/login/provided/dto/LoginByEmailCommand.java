package net.akive.api.auth.application.login.provided.dto;

import net.akive.api.common.domain.Email;

public record LoginByEmailCommand(Email email, String password) {

    public static LoginByEmailCommand of(String email, String password) {
        return new LoginByEmailCommand(new Email(email), password);
    }
}
