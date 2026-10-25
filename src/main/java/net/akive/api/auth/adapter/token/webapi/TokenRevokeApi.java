package net.akive.api.auth.adapter.token.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RestController
@RequestMapping("/auth/tokens")
@RequiredArgsConstructor
public class TokenRevokeApi {

    private final RefreshTokenSessionManager refreshTokenSessionManager;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

    @DeleteMapping("/refresh")
    public ResponseEntity<Void> revokeAllRefreshTokens(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie
    ) {
        refreshTokenSessionManager.revokeAll(refreshTokenCookieManager.read(refreshTokenCookie));

        return expireRefreshTokenCookie();
    }

    @DeleteMapping("/refresh/current")
    public ResponseEntity<Void> revokeCurrentRefreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie
    ) {
        refreshTokenSessionManager.revokeCurrent(refreshTokenCookieManager.read(refreshTokenCookie));

        return expireRefreshTokenCookie();
    }

    private ResponseEntity<Void> expireRefreshTokenCookie() {
        return ResponseEntity.noContent()
                .header(SET_COOKIE, refreshTokenCookieManager.expire().toString())
                .build();
    }
}
