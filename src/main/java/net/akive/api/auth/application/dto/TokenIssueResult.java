package net.akive.api.auth.application.dto;

public record TokenIssueResult(String accessToken, String refreshToken) {
}
