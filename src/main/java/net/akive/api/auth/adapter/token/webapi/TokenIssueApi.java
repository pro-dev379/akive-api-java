package net.akive.api.auth.adapter.token.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager;
import net.akive.api.auth.adapter.token.webapi.dto.response.TokenReissueResponse;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.domain.token.ReissuedToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RestController
@RequestMapping("/auth/tokens")
@RequiredArgsConstructor
public class TokenIssueApi {

    private final TokenIssuer tokenIssuer;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

    @PostMapping
    public ResponseEntity<TokenReissueResponse> reissue(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie
    ) {
        TokenReissueCommand command = new TokenReissueCommand(refreshTokenCookieManager.read(refreshTokenCookie));
        ReissuedToken token = tokenIssuer.reissue(command);
        ResponseEntity.BodyBuilder response = ResponseEntity.ok();

        if (token.rotatedRefreshToken() != null) {
            response.header(SET_COOKIE, refreshTokenCookieManager.create(token.rotatedRefreshToken()).toString());
        }

        return response.body(TokenReissueResponse.from(token));
    }
}
