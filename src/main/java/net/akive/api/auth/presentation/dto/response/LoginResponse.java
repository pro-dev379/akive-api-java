package net.akive.api.auth.presentation.dto.response;

import net.akive.api.auth.application.dto.result.login.LoginResult;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.refreshToken());
    }
}
