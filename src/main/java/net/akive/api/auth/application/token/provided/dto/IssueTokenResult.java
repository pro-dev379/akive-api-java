package net.akive.api.auth.application.token.provided.dto;

public record IssueTokenResult(String accessToken, String refreshToken) {
}
