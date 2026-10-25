package net.akive.api.auth.adapter.token.webapi;

import jakarta.servlet.http.Cookie;
import net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.domain.token.ReissuedToken;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME;
import static net.akive.api.auth.application.token.provided.dto.TokenReissueCommandFixture.createTokenReissueCommand;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.ReissuedTokenFixture.*;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;
import static net.akive.api.common.exception.business.AuthenticationError.REFRESH_TOKEN_EXPIRED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@WebMvcTest(TokenIssueApi.class)
@AutoConfigureMockMvc(addFilters = false)
class TokenIssueApiTest extends PublicApiTestSupport {

    private static final String TOKEN_REISSUE_URL = "/auth/tokens";

    @MockitoBean
    TokenIssuer tokenIssuer;

    @MockitoBean
    RefreshTokenCookieManager refreshTokenCookieManager;

    @Test
    void 리프레시_토큰으로_토큰을_재발급한다() {
        TokenReissueCommand command = createTokenReissueCommand();
        ReissuedToken reissuedToken = createReissuedToken();
        ResponseCookie refreshTokenCookie = ResponseCookie.from(
                REFRESH_TOKEN_COOKIE_NAME,
                reissuedToken.rotatedRefreshToken().value()
        ).build();

        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(command.refreshToken());
        given(tokenIssuer.reissue(command)).willReturn(reissuedToken);
        given(refreshTokenCookieManager.create(reissuedToken.rotatedRefreshToken())).willReturn(refreshTokenCookie);

        MvcTestResultAssert result = postWithRefreshTokenCookie().hasStatus(OK);

        result.hasHeader(SET_COOKIE, refreshTokenCookie.toString());
        result.bodyJson().extractingPath("$.accessToken").isEqualTo(REISSUED_ACCESS_TOKEN_VALUE);
        result.bodyText().doesNotContain("refreshToken");

        then(tokenIssuer).should().reissue(command);
        then(refreshTokenCookieManager).should().create(reissuedToken.rotatedRefreshToken());
    }

    @Test
    void 리프레시_토큰_교체가_필요하지_않으면_액세스_토큰만_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();
        ReissuedToken reissuedToken = createReissuedTokenWithoutRotatedRefreshToken();

        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(command.refreshToken());
        given(tokenIssuer.reissue(command)).willReturn(reissuedToken);

        MvcTestResultAssert result = postWithRefreshTokenCookie().hasStatus(OK);

        result.bodyJson().extractingPath("$.accessToken").isEqualTo(REISSUED_ACCESS_TOKEN_VALUE);
        result.bodyText().doesNotContain("refreshToken");

        then(tokenIssuer).should().reissue(command);
        then(refreshTokenCookieManager).should(never()).create(any());
    }

    @Test
    void 리프레시_토큰이_유효하지_않으면_401을_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();

        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(command.refreshToken());
        willThrow(new AuthenticationException(INVALID_REFRESH_TOKEN))
                .given(tokenIssuer)
                .reissue(command);

        MvcTestResultAssert result = postWithRefreshTokenCookie().hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_REFRESH_TOKEN);

        then(tokenIssuer).should().reissue(command);
        then(refreshTokenCookieManager).should(never()).create(any());
    }

    @Test
    void 리프레시_토큰이_만료되면_401을_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();

        given(refreshTokenCookieManager.read(REFRESH_TOKEN_VALUE)).willReturn(command.refreshToken());
        willThrow(new AuthenticationException(REFRESH_TOKEN_EXPIRED))
                .given(tokenIssuer)
                .reissue(command);

        MvcTestResultAssert result = postWithRefreshTokenCookie().hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, REFRESH_TOKEN_EXPIRED);

        then(tokenIssuer).should().reissue(command);
        then(refreshTokenCookieManager).should(never()).create(any());
    }

    @Test
    void 리프레시_토큰_쿠키가_없으면_401을_응답한다() {
        given(refreshTokenCookieManager.read(null))
                .willThrow(new AuthenticationException(INVALID_REFRESH_TOKEN));

        MvcTestResultAssert result = mvcTester.post()
                .uri(TOKEN_REISSUE_URL)
                .assertThat()
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_REFRESH_TOKEN);

        then(tokenIssuer).shouldHaveNoInteractions();
    }

    private MvcTestResultAssert postWithRefreshTokenCookie() {
        return mvcTester.post()
                .uri(TOKEN_REISSUE_URL)
                .cookie(new Cookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN_VALUE))
                .assertThat();
    }
}
