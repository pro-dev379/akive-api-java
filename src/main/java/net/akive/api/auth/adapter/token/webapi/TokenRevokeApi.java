package net.akive.api.auth.adapter.token.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.core.config.security.AuthenticatedUserId;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/auth/tokens")
@RequiredArgsConstructor
public class TokenRevokeApi {

    private final RefreshTokenSessionManager refreshTokenSessionManager;

    @DeleteMapping("/refresh")
    @ResponseStatus(NO_CONTENT)
    public void revokeAllRefreshTokens(@AuthenticatedUserId Long userId) {
        refreshTokenSessionManager.revokeByUserId(userId);
    }

    @DeleteMapping("/refresh/current")
    @ResponseStatus(NO_CONTENT)
    public void revokeCurrentRefreshToken(
            @RequestHeader("X-Client-Instance-Id") UUID clientInstanceIdHeader,
            @AuthenticatedUserId Long userId
    ) {
        refreshTokenSessionManager.revokeByUserIdAndClientInstanceId(
                userId,
                new ClientInstanceId(clientInstanceIdHeader)
        );
    }
}
