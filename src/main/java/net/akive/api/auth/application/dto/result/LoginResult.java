package net.akive.api.auth.application.dto.result;

public record LoginResult(
        String accessToken,
        String refreshToken
) {

    public static LoginResult of(String accessToken, String refreshToken) {
        return new LoginResult(accessToken, refreshToken);
    }
}
