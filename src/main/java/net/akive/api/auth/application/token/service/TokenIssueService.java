package net.akive.api.auth.application.token.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.application.token.required.AccessTokenCreator;
import net.akive.api.auth.application.token.required.RefreshTokenParser;
import net.akive.api.auth.domain.token.*;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.user.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class TokenIssueService implements TokenIssuer {

    private final RefreshTokenSessionManager refreshTokenSessionManager;

    private final AccessTokenCreator accessTokenCreator;
    private final RefreshTokenParser refreshTokenParser;

    @Override
    public IssuedToken issue(TokenIssueCommand command) {
        AccessToken accessToken = accessTokenCreator.create(command.userId(), command.role());
        RefreshToken refreshToken = refreshTokenSessionManager.start(
                command.userId(),
                command.clientInstanceId(),
                command.role()
        );

        return new IssuedToken(accessToken, refreshToken);
    }

    @Override
    public ReissuedToken reissue(TokenReissueCommand command) throws AuthenticationException {
        RefreshTokenPayload refreshTokenPayload = refreshTokenParser.parse(command.refreshToken());

        Long userId = refreshTokenPayload.userId();
        UserRole role = refreshTokenPayload.role();

        AccessToken newAccessToken = accessTokenCreator.create(userId, role);
        RefreshToken newRefreshToken = refreshTokenSessionManager
                .rotateIfRequired(refreshTokenPayload, command.refreshToken())
                .orElse(null);

        return new ReissuedToken(newAccessToken, newRefreshToken);
    }
}
