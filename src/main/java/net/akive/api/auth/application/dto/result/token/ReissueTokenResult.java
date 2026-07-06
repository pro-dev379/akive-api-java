package net.akive.api.auth.application.dto.result.token;

import org.springframework.lang.Nullable;

public record ReissueTokenResult(String accessToken, @Nullable String refreshToken) {
}
