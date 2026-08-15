package net.akive.api.auth.domain.token;

public class RefreshTokenFixture {

    public static final String REFRESH_TOKEN_VALUE = "refresh-token";

    public static RefreshToken createRefreshToken() {
        return createRefreshToken(REFRESH_TOKEN_VALUE);
    }

    public static RefreshToken createRefreshToken(String value) {
        return new RefreshToken(value);
    }
}
