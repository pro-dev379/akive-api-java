package net.akive.api.auth.adapter.token.webapi;

import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.domain.token.ReissuedToken;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.auth.application.token.provided.dto.TokenReissueCommandFixture.createTokenReissueCommand;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.ReissuedTokenFixture.*;
import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_REFRESH_TOKEN;
import static net.akive.api.common.exception.business.UnauthorizedError.REFRESH_TOKEN_EXPIRED;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class TokenApiTest extends PublicApiTestSupport {

    @Test
    void 리프레시_토큰으로_토큰을_재발급한다() {
        TokenReissueCommand command = createTokenReissueCommand();
        ReissuedToken reissuedToken = createReissuedToken();

        given(tokenIssuer.reissue(command)).willReturn(reissuedToken);

        MvcTestResultAssert result =
                postTokenReissue("Bearer " + REFRESH_TOKEN_VALUE).hasStatus(OK);

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
                postTokenReissue("Bearer " + REFRESH_TOKEN_VALUE).hasStatus(OK);

        result.bodyJson().extractingPath("$.accessToken").isEqualTo(REISSUED_ACCESS_TOKEN_VALUE);
        result.bodyText().doesNotContain("refreshToken");

        then(tokenIssuer).should().reissue(command);
    }

    @Test
    void 리프레시_토큰이_유효하지_않으면_401을_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();

        willThrow(new UnauthorizedException(INVALID_REFRESH_TOKEN))
                .given(tokenIssuer)
                .reissue(command);

        postTokenReissue("Bearer " + REFRESH_TOKEN_VALUE).hasStatus(UNAUTHORIZED);

        then(tokenIssuer).should().reissue(command);
    }

    @Test
    void 리프레시_토큰이_만료되면_401을_응답한다() {
        TokenReissueCommand command = createTokenReissueCommand();

        willThrow(new UnauthorizedException(REFRESH_TOKEN_EXPIRED))
                .given(tokenIssuer)
                .reissue(command);

        postTokenReissue("Bearer " + REFRESH_TOKEN_VALUE).hasStatus(UNAUTHORIZED);

        then(tokenIssuer).should().reissue(command);
    }

    private MvcTestResultAssert postTokenReissue(String authorizationHeader) {
        return mvcTester.post()
                .uri("/tokens")
                .header("Authorization", authorizationHeader)
                .assertThat();
    }
}
