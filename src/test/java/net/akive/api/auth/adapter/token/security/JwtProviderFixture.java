package net.akive.api.auth.adapter.token.security;

import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import net.akive.api.core.config.security.JwtProperties;

import java.time.Instant;

class JwtProviderFixture {

    static final String ISSUER = "akive";
    static final Long USER_ID = 1L;
    static final Instant EXPIRES_AT = Instant.parse("2026-08-29T12:00:00Z");
    static final ClientInstanceId CLIENT_INSTANCE_ID =
            ClientInstanceId.parse("019ff948-3a43-73f0-b263-792a14573170");
    static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 30;
    static final long REFRESH_TOKEN_EXPIRATION_DAYS = 14;
    static final Instant NOW = Instant.parse("2026-08-15T12:00:00Z");

    static JwtProperties jwtProperties() {
        return new JwtProperties(
                ISSUER,
                new JwtProperties.AccessTokenProperties(
                        "access-secret",
                        ACCESS_TOKEN_EXPIRATION_MINUTES
                ),
                new JwtProperties.RefreshTokenProperties(
                        "refresh-secret",
                        REFRESH_TOKEN_EXPIRATION_DAYS
                )
        );
    }

    static FixedCurrentTimeProvider fixedCurrentTimeProvider() {
        return new FixedCurrentTimeProvider(NOW);
    }
}
