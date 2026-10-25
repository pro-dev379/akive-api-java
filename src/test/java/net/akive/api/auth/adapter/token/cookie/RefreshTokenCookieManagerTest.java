package net.akive.api.auth.adapter.token.cookie;

import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.core.config.security.RefreshTokenCookieProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RefreshTokenCookieManagerTest {

    private static final String EXPECTED_REFRESH_TOKEN_COOKIE_NAME = "__Host-akive_refresh_token";

    @Test
    void 리프레시_토큰을_Host_접두사_조건을_갖춘_HttpOnly_쿠키로_생성한다() {
        RefreshTokenCookieManager cookieManager = createCookieManagerWithLaxSameSite();
        ResponseCookie cookie = cookieManager.create(createRefreshToken());

        assertThat(cookie.getName()).isEqualTo(EXPECTED_REFRESH_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEqualTo(REFRESH_TOKEN_VALUE);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(14));
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getDomain()).isNull();
    }

    @Test
    void 리프레시_토큰_쿠키를_즉시_만료시킨다() {
        RefreshTokenCookieManager cookieManager = createCookieManagerWithLaxSameSite();
        ResponseCookie cookie = cookieManager.expire();

        assertThat(cookie.getName()).isEqualTo(EXPECTED_REFRESH_TOKEN_COOKIE_NAME);
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isZero();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getDomain()).isNull();
    }

    @Test
    void 리프레시_토큰_쿠키를_읽을_때_값이_없거나_비어_있으면_예외를_던진다() {
        RefreshTokenCookieManager cookieManager = createCookieManagerWithLaxSameSite();

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> cookieManager.read(null))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);
        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> cookieManager.read(" "))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    private RefreshTokenCookieManager createCookieManagerWithLaxSameSite() {
        RefreshTokenCookieProperties refreshTokenCookieProperties =
                new RefreshTokenCookieProperties("Lax");
        JwtProperties jwtProperties = new JwtProperties(
                "akive",
                new JwtProperties.AccessTokenProperties("access-token-secret", 30),
                new JwtProperties.RefreshTokenProperties("refresh-token-secret", 14)
        );

        return new RefreshTokenCookieManager(refreshTokenCookieProperties, jwtProperties);
    }
}
