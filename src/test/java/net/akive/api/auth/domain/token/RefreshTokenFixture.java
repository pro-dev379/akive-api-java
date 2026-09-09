package net.akive.api.auth.domain.token;

import java.time.Duration;
import java.time.Instant;

public class RefreshTokenFixture {

    public static final String REFRESH_TOKEN_VALUE = "refresh-token";
    public static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(14);
    public static final Instant EXPIRE_AT = Instant.parse("2026-07-03T00:01:00Z");

    public static RefreshToken createRefreshToken() {
        return createRefreshToken(REFRESH_TOKEN_VALUE);
    }

    public static RefreshToken createRefreshToken(String value) {
        return new RefreshToken(value);
    }
}
