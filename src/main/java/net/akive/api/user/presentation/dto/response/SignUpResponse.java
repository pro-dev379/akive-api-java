package net.akive.api.user.presentation.dto.response;

import net.akive.api.user.application.dto.SignUpResult;

public record SignUpResponse(
        String nickname,
        String accessToken,
        String refreshToken
) {

    public static SignUpResponse from(SignUpResult result) {
        return new SignUpResponse(result.nickname(), result.accessToken(), result.refreshToken());
    }
}
