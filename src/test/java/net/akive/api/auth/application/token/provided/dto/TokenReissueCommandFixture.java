package net.akive.api.auth.application.token.provided.dto;

import net.akive.api.auth.domain.token.RefreshToken;

import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;

public class TokenReissueCommandFixture {

    public static TokenReissueCommand createTokenReissueCommand() {
        return createTokenReissueCommand(createRefreshToken());
    }

    public static TokenReissueCommand createTokenReissueCommand(RefreshToken refreshToken) {
        return new TokenReissueCommand(refreshToken);
    }
}
