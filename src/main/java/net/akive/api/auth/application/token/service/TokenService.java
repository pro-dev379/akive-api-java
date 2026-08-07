package net.akive.api.auth.application.token.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.provided.dto.IssueTokenResult;
import net.akive.api.auth.application.token.provided.dto.ReissueTokenResult;
import net.akive.api.auth.application.token.provided.IssueTokenUseCase;
import net.akive.api.auth.application.token.required.LoadTokenPort;
import net.akive.api.auth.application.token.required.RotateTokenPort;
import net.akive.api.auth.application.token.required.SaveTokenPort;
import net.akive.api.auth.application.token.required.CreateTokenPort;
import net.akive.api.auth.application.token.required.ValidateTokenPort;
import net.akive.api.auth.domain.token.TokenPayload;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_REFRESH_TOKEN;

@Service
@Transactional
@RequiredArgsConstructor
class TokenService implements IssueTokenUseCase {

    private final LoadTokenPort loadTokenPort;
    private final CreateTokenPort createTokenPort;
    private final SaveTokenPort saveTokenPort;
    private final RotateTokenPort rotateTokenPort;
    private final ValidateTokenPort validateTokenPort;

    private final CurrentTimeProvider currentTimeProvider;
    private final JwtProperties jwtProperties;

    @Override
    public IssueTokenResult issue(Long userId, RoleType roleType) {
        String accessToken = createTokenPort.createAccessToken(userId, roleType);
        String refreshToken = createTokenPort.createRefreshToken(userId, roleType);

        saveTokenPort.save(
                userId,
                refreshToken,
                jwtProperties.refreshToken().expiration()
        );

        return new IssueTokenResult(accessToken, refreshToken);
    }

    @Override
    public ReissueTokenResult reissue(String refreshToken) {
        TokenPayload tokenPayload = validateTokenPort.validateRefreshToken(refreshToken);

        Long userId = tokenPayload.userId();
        RoleType roleType = tokenPayload.roleType();
        Instant expiresAt = tokenPayload.expiresAt();

        validateSavedRefreshToken(userId, refreshToken);

        String newAccessToken = createTokenPort.createAccessToken(userId, roleType);
        String newRefreshToken = rotateRefreshTokenIfRequired(userId, roleType, refreshToken, expiresAt);

        return new ReissueTokenResult(newAccessToken, newRefreshToken);
    }

    private void validateSavedRefreshToken(Long userId, String refreshToken) {
        String savedRefreshToken = loadTokenPort
                .loadByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN));

        if (!savedRefreshToken.equals(refreshToken)) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
    }

    private String rotateRefreshTokenIfRequired(
            Long userId,
            RoleType roleType,
            String refreshToken,
            Instant expiresAt
    ) {
        if (!requiresRefreshTokenRotation(expiresAt)) {
            return null;
        }

        String newRefreshToken = createTokenPort.createRefreshToken(userId, roleType);
        boolean rotated = rotateTokenPort.rotate(
                userId,
                refreshToken,
                newRefreshToken,
                jwtProperties.refreshToken().expiration()
        );

        if (!rotated) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }

        return newRefreshToken;
    }

    private boolean requiresRefreshTokenRotation(Instant expiresAt) {
        Instant now = currentTimeProvider.instant();
        Duration remaining = Duration.between(now, expiresAt);

        return !remaining.isNegative()
                && remaining.compareTo(Duration.ofMinutes(1)) <= 0;
    }
}
