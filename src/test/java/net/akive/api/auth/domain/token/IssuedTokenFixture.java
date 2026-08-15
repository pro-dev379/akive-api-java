package net.akive.api.auth.domain.token;

import static net.akive.api.auth.domain.token.AccessTokenFixture.ACCESS_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.AccessTokenFixture.createAccessToken;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;

public class IssuedTokenFixture {

    public static IssuedToken createIssuedToken() {
        return createIssuedToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE);
    }

    public static IssuedToken createIssuedToken(String accessToken, String refreshToken) {
        return new IssuedToken(createAccessToken(accessToken), createRefreshToken(refreshToken));
    }
}
