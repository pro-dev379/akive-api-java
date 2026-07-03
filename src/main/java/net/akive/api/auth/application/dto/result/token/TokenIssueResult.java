package net.akive.api.auth.application.dto.result.token;

public record TokenIssueResult(String accessToken, String refreshToken) {
}
