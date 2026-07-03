package net.akive.api.auth.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.result.token.TokenReissueResult;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.domain.token.exception.TokenException;
import net.akive.api.auth.presentation.dto.response.TokenReissueResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.akive.api.auth.domain.token.exception.TokenErrorCode.INVALID_REFRESH_TOKEN;

@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor
public class TokenController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final IssueTokenUseCase issueTokenUseCase;

    @PostMapping
    public TokenReissueResponse reissue(@RequestHeader("Authorization") String authorizationHeader) {
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new TokenException(INVALID_REFRESH_TOKEN);
        }

        String refreshToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (refreshToken.isBlank()) {
            throw new TokenException(INVALID_REFRESH_TOKEN);
        }

        TokenReissueResult issue = issueTokenUseCase.reissue(refreshToken);

        return new TokenReissueResponse(issue.accessToken(), issue.refreshToken());
    }
}
