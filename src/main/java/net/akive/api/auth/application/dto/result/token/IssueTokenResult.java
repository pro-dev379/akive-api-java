package net.akive.api.auth.application.dto.result.token;

public record IssueTokenResult(String accessToken, String refreshToken) {
}
