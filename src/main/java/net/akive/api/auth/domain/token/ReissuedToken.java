package net.akive.api.auth.domain.token;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record ReissuedToken(AccessToken accessToken, RefreshToken rotatedRefreshToken) {

    public ReissuedToken {
        requireNonNull(accessToken);
    }
}
