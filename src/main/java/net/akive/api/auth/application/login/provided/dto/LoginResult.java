package net.akive.api.auth.application.login.provided.dto;

public record LoginResult(
        String accessToken,
        String refreshToken
) {
}
