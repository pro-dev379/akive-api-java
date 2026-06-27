package net.akive.api.auth.application.dto.result;

public record TokenIssueResult(String accessToken, String refreshToken) {
}
