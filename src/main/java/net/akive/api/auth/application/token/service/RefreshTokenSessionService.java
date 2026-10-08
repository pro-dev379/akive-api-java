package net.akive.api.auth.application.token.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.provided.RefreshTokenSessionManager;
import net.akive.api.auth.application.token.required.RefreshTokenCreator;
import net.akive.api.auth.application.token.required.RefreshTokenStore;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;

@Service
@Transactional
@RequiredArgsConstructor
class RefreshTokenSessionService implements RefreshTokenSessionManager {

    private final RefreshTokenCreator refreshTokenCreator;
    private final RefreshTokenStore refreshTokenStore;

    private final RefreshTokenRotationPolicy refreshTokenRotationPolicy;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshToken start(Long userId, ClientInstanceId clientInstanceId, UserRole role) {
        RefreshToken refreshToken = refreshTokenCreator.create(userId, clientInstanceId, role);

        refreshTokenStore.save(
                userId,
                clientInstanceId,
                refreshToken,
                jwtProperties.refreshToken().expiration()
        );

        return refreshToken;
    }

    public Optional<RefreshToken> rotateIfRequired(
            RefreshTokenPayload payload,
            RefreshToken currentRefreshToken
    ) throws AuthenticationException {
        validateCurrent(payload.userId(), payload.clientInstanceId(), currentRefreshToken);

        if (!refreshTokenRotationPolicy.requiresRotation(payload.expiresAt())) {
            return Optional.empty();
        }

        RefreshToken newRefreshToken = refreshTokenCreator.create(
                payload.userId(),
                payload.clientInstanceId(),
                payload.role()
        );

        boolean rotated = refreshTokenStore.rotate(
                payload.userId(),
                payload.clientInstanceId(),
                currentRefreshToken,
                newRefreshToken,
                jwtProperties.refreshToken().expiration()
        );

        if (!rotated) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }

        return Optional.of(newRefreshToken);
    }

    @Override
    public void revokeByUserId(Long userId) {
        refreshTokenStore.deleteByUserId(userId);
    }

    @Override
    public void revokeByUserIdAndClientInstanceId(Long userId, ClientInstanceId clientInstanceId) {
        refreshTokenStore.deleteByUserIdAndClientInstanceId(userId, clientInstanceId);
    }

    private void validateCurrent(Long userId, ClientInstanceId clientInstanceId, RefreshToken refreshToken) {
        RefreshToken savedRefreshToken = refreshTokenStore
                .findByUserIdAndClientInstantId(userId, clientInstanceId)
                .orElseThrow(() -> new AuthenticationException(INVALID_REFRESH_TOKEN));

        if (!savedRefreshToken.equals(refreshToken)) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }
    }
}
