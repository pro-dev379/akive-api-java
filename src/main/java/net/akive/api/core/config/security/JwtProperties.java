package net.akive.api.core.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        AccessTokenProperties accessToken,
        RefreshTokenProperties refreshToken
) {

    public record AccessTokenProperties(
            String secret,
            long expirationMinutes
    ) {
    }

    public record RefreshTokenProperties(
            String secret,
            long expirationDays
    ) {

        public Duration expiration() {
            return Duration.ofDays(expirationDays);
        }
    }
}
