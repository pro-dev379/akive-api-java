package net.akive.api.auth.application.dto.result.login;

public record LoginResult(
        String accessToken,
        String refreshToken
) {
}
