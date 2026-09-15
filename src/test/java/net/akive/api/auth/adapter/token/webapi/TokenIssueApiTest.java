package net.akive.api.auth.adapter.token.webapi;

import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.domain.token.ReissuedToken;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.auth.application.token.provided.dto.TokenReissueCommandFixture.createTokenReissueCommand;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.ReissuedTokenFixture.*;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;
import static net.akive.api.common.exception.business.AuthenticationError.REFRESH_TOKEN_EXPIRED;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@WebMvcTest(TokenIssueApi.class)
@AutoConfigureMockMvc(addFilters = false)
class TokenIssueApiTest extends PublicApiTestSupport {

    private static final String TOKEN_REISSUE_URL = "/auth/tokens";

    @MockitoBean
    TokenIssuer tokenIssuer;

    @Test
    void 리프레시_토큰으로_토큰을_재발급한다() {
        TokenReissueCommand command = createTokenReissueCommand();
        ReissuedToken reissuedToken = createReissuedToken();

        given(tokenIssuer.reissue(command)).willReturn(reissuedToken);

        MvcTestResultAssert result =
                postWithAuthorization(TOKEN_REISSUE_URL, "Bearer " + REFRESH_TOKEN_VALUE).hasStatus(OK);

        result.bodyJson().extractingPath("$.accessToken").isEqualTo(REISSUED_ACCESS_TOKEN_VALUE);
        result.bodyJson().extractingPath("$.refreshToken").isEqualTo(ROTATED_REFRESH_TOKEN_VALUE);

        then(tokenIssuer).should().reissue(command);
    }

    @Test
    void 리프레시_토큰_교체가_필요하지_않으면_액세스_토큰만_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();
        ReissuedToken reissuedToken = createReissuedTokenWithoutRotatedRefreshToken();

        given(tokenIssuer.reissue(command)).willReturn(reissuedToken);

        MvcTestResultAssert result =
                postWithAuthorization(TOKEN_REISSUE_URL, "Bearer " + REFRESH_TOKEN_VALUE).hasStatus(OK);

        result.bodyJson().extractingPath("$.accessToken").isEqualTo(REISSUED_ACCESS_TOKEN_VALUE);
        result.bodyText().doesNotContain("refreshToken");

        then(tokenIssuer).should().reissue(command);
    }

    @Test
    void 리프레시_토큰이_유효하지_않으면_401을_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();

        willThrow(new AuthenticationException(INVALID_REFRESH_TOKEN))
                .given(tokenIssuer)
                .reissue(command);

        MvcTestResultAssert result =
                postWithAuthorization(TOKEN_REISSUE_URL, "Bearer " + REFRESH_TOKEN_VALUE).hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_REFRESH_TOKEN);

        then(tokenIssuer).should().reissue(command);
    }

    @Test
    void 리프레시_토큰이_만료되면_401을_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();

        willThrow(new AuthenticationException(REFRESH_TOKEN_EXPIRED))
                .given(tokenIssuer)
                .reissue(command);

        MvcTestResultAssert result =
                postWithAuthorization(TOKEN_REISSUE_URL, "Bearer " + REFRESH_TOKEN_VALUE).hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, REFRESH_TOKEN_EXPIRED);

        then(tokenIssuer).should().reissue(command);
    }
}
