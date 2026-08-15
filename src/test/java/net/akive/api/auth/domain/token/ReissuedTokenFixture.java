package net.akive.api.auth.domain.token;

import static net.akive.api.auth.domain.token.AccessTokenFixture.createAccessToken;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;

public class ReissuedTokenFixture {

    public static final String REISSUED_ACCESS_TOKEN_VALUE = "new-access-token";
    public static final String ROTATED_REFRESH_TOKEN_VALUE = "new-refresh-token";

    public static ReissuedToken createReissuedToken() {
        return createReissuedToken(REISSUED_ACCESS_TOKEN_VALUE, ROTATED_REFRESH_TOKEN_VALUE);
    }

    public static ReissuedToken createReissuedTokenWithoutRotatedRefreshToken() {
        return new ReissuedToken(createAccessToken(REISSUED_ACCESS_TOKEN_VALUE), null);
    }

    public static ReissuedToken createReissuedToken(String accessToken, String rotatedRefreshToken) {
        return new ReissuedToken(createAccessToken(accessToken), createRefreshToken(rotatedRefreshToken));
    }
}
