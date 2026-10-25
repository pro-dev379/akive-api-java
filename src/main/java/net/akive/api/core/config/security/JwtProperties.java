package net.akive.api.core.config.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotNull @Valid AccessTokenProperties accessToken,
        @NotNull @Valid RefreshTokenProperties refreshToken
) {

    public record AccessTokenProperties(
            @NotBlank @Size(min = 32) String secret,
            @Positive long expirationMinutes
    ) {
    }

    public record RefreshTokenProperties(
            @NotBlank @Size(min = 32) String secret,
            @Positive long expirationDays
    ) {

        public Duration expiration() {
            return Duration.ofDays(expirationDays);
        }
    }
}
