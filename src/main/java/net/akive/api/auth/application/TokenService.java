package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.TokenIssueResult;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.application.port.out.token.RefreshTokenRepository;
import net.akive.api.auth.application.port.out.token.TokenGenerator;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
class TokenService implements IssueTokenUseCase {

    private final TokenGenerator tokenGenerator;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    public TokenIssueResult issue(Long userId, RoleType roleType) {
        String accessToken = tokenGenerator.createAccessToken(userId, roleType);
        String refreshToken = tokenGenerator.createRefreshToken(userId, roleType);
        long refreshTokenExpiration = jwtProperties.refreshToken().expirationDays();

        refreshTokenRepository.save(
                userId,
                refreshToken,
                Duration.ofDays(refreshTokenExpiration)
        );

        return new TokenIssueResult(accessToken, refreshToken);
    }
}
