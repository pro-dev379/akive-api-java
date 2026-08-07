package net.akive.api.auth.application.token.provided.dto;

import org.springframework.lang.Nullable;

public record ReissueTokenResult(String accessToken, @Nullable String refreshToken) {
}
