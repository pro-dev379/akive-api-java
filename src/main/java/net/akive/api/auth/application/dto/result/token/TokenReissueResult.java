package net.akive.api.auth.application.dto.result.token;

import org.springframework.lang.Nullable;

public record TokenReissueResult(String accessToken, @Nullable String refreshToken) {
}
