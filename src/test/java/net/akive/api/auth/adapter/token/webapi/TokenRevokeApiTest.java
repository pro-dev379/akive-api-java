package net.akive.api.auth.adapter.token.webapi;

import jakarta.servlet.http.Cookie;
import net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@WebMvcTest(TokenRevokeApi.class)
@AutoConfigureMockMvc(addFilters = false)
class TokenRevokeApiTest extends PublicApiTestSupport {

    private static final String REFRESH_TOKEN_REVOKE_URL = "/auth/tokens/refresh";
    private static final String CURRENT_REFRESH_TOKEN_REVOKE_URL = "/auth/tokens/refresh/current";

    @MockitoBean
    RefreshTokenSessionManager refreshTokenSessionManager;

    @MockitoBean
    RefreshTokenCookieManager refreshTokenCookieManager;

    @Test
    void 리프레시_토큰으로_회원의_모든_리프레시_토큰_세션을_무효화하고_쿠키를_삭제한다() {
        RefreshToken refreshToken = createRefreshToken();
        ResponseCookie expiredCookie = expiredRefreshTokenCookie();
        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(refreshToken);
        given(refreshTokenCookieManager.expire()).willReturn(expiredCookie);

        deleteWithRefreshTokenCookie(REFRESH_TOKEN_REVOKE_URL)
                .hasStatus(NO_CONTENT)
                .hasHeader(SET_COOKIE, expiredCookie.toString());

        then(refreshTokenSessionManager).should().revokeAll(refreshToken);
        then(refreshTokenCookieManager).should().expire();
    }

    @Test
    void 리프레시_토큰으로_현재_클라이언트의_리프레시_토큰_세션만_무효화하고_쿠키를_삭제한다() {
        RefreshToken refreshToken = createRefreshToken();
        ResponseCookie expiredCookie = expiredRefreshTokenCookie();
        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(refreshToken);
        given(refreshTokenCookieManager.expire()).willReturn(expiredCookie);

        deleteWithRefreshTokenCookie(CURRENT_REFRESH_TOKEN_REVOKE_URL)
                .hasStatus(NO_CONTENT)
                .hasHeader(SET_COOKIE, expiredCookie.toString());

        then(refreshTokenSessionManager).should().revokeCurrent(refreshToken);
        then(refreshTokenCookieManager).should().expire();
    }

    @ParameterizedTest
    @ValueSource(strings = {REFRESH_TOKEN_REVOKE_URL, CURRENT_REFRESH_TOKEN_REVOKE_URL})
    void 리프레시_토큰_쿠키가_없으면_401을_응답한다(String url) {
        given(refreshTokenCookieManager.read(null))
                .willThrow(new AuthenticationException(INVALID_REFRESH_TOKEN));

        MvcTestResultAssert result = mvcTester.delete()
                .uri(url)
                .assertThat()
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_REFRESH_TOKEN);
        then(refreshTokenSessionManager).shouldHaveNoInteractions();
    }

    @Test
    void 모든_세션_무효화_시_리프레시_토큰이_유효하지_않으면_401을_응답하고_쿠키를_변경하지_않는다() {
        RefreshToken refreshToken = createRefreshToken();
        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(refreshToken);
        willThrow(new AuthenticationException(INVALID_REFRESH_TOKEN))
                .given(refreshTokenSessionManager)
                .revokeAll(refreshToken);

        MvcTestResultAssert result = deleteWithRefreshTokenCookie(REFRESH_TOKEN_REVOKE_URL)
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_REFRESH_TOKEN);
        then(refreshTokenCookieManager).should(never()).expire();
    }

    @Test
    void 현재_세션_무효화_시_리프레시_토큰이_유효하지_않으면_401을_응답하고_쿠키를_변경하지_않는다() {
        RefreshToken refreshToken = createRefreshToken();
        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(refreshToken);
        willThrow(new AuthenticationException(INVALID_REFRESH_TOKEN))
                .given(refreshTokenSessionManager)
                .revokeCurrent(refreshToken);

        MvcTestResultAssert result = deleteWithRefreshTokenCookie(CURRENT_REFRESH_TOKEN_REVOKE_URL)
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_REFRESH_TOKEN);
        then(refreshTokenCookieManager).should(never()).expire();
    }

    private MvcTestResultAssert deleteWithRefreshTokenCookie(String url) {
        return mvcTester.delete()
                .uri(url)
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_VALUE))
                .assertThat();
    }

    private ResponseCookie expiredRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "").build();
    }
}
