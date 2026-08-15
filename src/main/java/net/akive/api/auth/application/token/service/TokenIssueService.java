package net.akive.api.auth.application.token.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.auth.application.token.provided.TokenIssuer;
import net.akive.api.auth.application.token.provided.dto.TokenIssueCommand;
import net.akive.api.auth.application.token.provided.dto.TokenReissueCommand;
import net.akive.api.auth.application.token.required.AccessTokenCreator;
import net.akive.api.auth.application.token.required.RefreshTokenParser;
import net.akive.api.auth.domain.token.*;
import net.akive.api.user.domain.RoleType;
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
        AccessToken accessToken = accessTokenCreator.create(command.userId(), command.roleType());
        RefreshToken refreshToken = refreshTokenSessionManager.start(
                command.userId(),
                command.clientInstanceId(),
                command.roleType()
        );

        return new IssuedToken(accessToken, refreshToken);
    }

    @Override
    public ReissuedToken reissue(TokenReissueCommand command) {
        RefreshTokenPayload refreshTokenPayload = refreshTokenParser.parse(command.refreshToken());

        Long userId = refreshTokenPayload.userId();
        RoleType roleType = refreshTokenPayload.roleType();

        AccessToken newAccessToken = accessTokenCreator.create(userId, roleType);
        RefreshToken newRefreshToken = refreshTokenSessionManager
                .rotateIfRequired(refreshTokenPayload, command.refreshToken())
                .orElse(null);

        return new ReissuedToken(newAccessToken, newRefreshToken);
    }
}
