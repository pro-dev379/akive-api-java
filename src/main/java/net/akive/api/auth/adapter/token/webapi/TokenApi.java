package net.akive.api.auth.adapter.token.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.adapter.token.webapi.dto.response.TokenReissueResponse;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.ReissuedToken;
import net.akive.api.common.exception.business.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;

@RestController
@RequestMapping("/auth/tokens")
@RequiredArgsConstructor
public class TokenApi {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenIssuer tokenIssuer;

    @PostMapping
    public TokenReissueResponse reissue(@RequestHeader("Authorization") String authorizationHeader) {
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }

        String refreshToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (refreshToken.isBlank()) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }

        TokenReissueCommand command = new TokenReissueCommand(new RefreshToken(refreshToken));

        ReissuedToken token = tokenIssuer.reissue(command);

        return TokenReissueResponse.from(token);
    }
}
