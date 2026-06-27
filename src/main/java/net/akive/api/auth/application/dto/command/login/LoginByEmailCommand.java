package net.akive.api.auth.application.dto.command.login;

public record LoginByEmailCommand(String email, String password) {

    public static LoginByEmailCommand of(String email, String password) {
        return new LoginByEmailCommand(email, password);
    }
}
