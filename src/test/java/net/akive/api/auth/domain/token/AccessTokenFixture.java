package net.akive.api.auth.domain.token;

public class AccessTokenFixture {

    public static final String ACCESS_TOKEN_VALUE = "access-token";

    public static AccessToken createAccessToken() {
        return createAccessToken(ACCESS_TOKEN_VALUE);
    }

    public static AccessToken createAccessToken(String value) {
        return new AccessToken(value);
    }
}
