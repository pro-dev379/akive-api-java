package net.akive.api.user.application.provided.dto;

public record SignUpResult(
        String nickname,
        String accessToken,
        String refreshToken
) {
}
