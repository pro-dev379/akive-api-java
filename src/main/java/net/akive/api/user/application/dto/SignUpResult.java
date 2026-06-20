package net.akive.api.user.application.dto;

public record SignUpResult(
        String nickname,
        String accessToken,
        String refreshToken
) {
}
