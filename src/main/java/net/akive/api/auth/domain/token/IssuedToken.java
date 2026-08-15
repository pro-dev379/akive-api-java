package net.akive.api.auth.domain.token;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record IssuedToken(AccessToken accessToken, RefreshToken refreshToken) {

    public IssuedToken {
        requireNonNull(accessToken);
        requireNonNull(refreshToken);
    }
}
