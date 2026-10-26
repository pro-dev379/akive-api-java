package net.akive.api.auth.adapter.token.cookie;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.core.config.security.RefreshTokenCookieExpirer;
import net.akive.api.core.config.security.RefreshTokenCookieProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieManager implements RefreshTokenCookieExpirer {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "__Host-akive_refresh_token";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/";

    private final RefreshTokenCookieProperties refreshTokenCookieProperties;
    private final JwtProperties jwtProperties;

    public ResponseCookie create(RefreshToken refreshToken) {
        return createCookie(
                refreshToken.value(),
                jwtProperties.refreshToken().expiration()
        );
    }

    public RefreshToken read(String value) {
        if (value == null || value.isBlank()) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }

        return new RefreshToken(value);
    }

    @Override
    public ResponseCookie expire() {
        return createCookie("", Duration.ZERO);
    }

    private ResponseCookie createCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite(refreshTokenCookieProperties.sameSite())
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
