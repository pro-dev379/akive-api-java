package net.akive.api.auth.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.dto.TokenIssueResult;
import net.akive.api.auth.application.port.in.token.IssueTokenUseCase;
import net.akive.api.auth.application.port.out.token.SaveTokenPort;
import net.akive.api.auth.application.port.out.token.CreateTokenPort;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class TokenService implements IssueTokenUseCase {

    private final CreateTokenPort createTokenPort;
    private final SaveTokenPort saveTokenPort;
    private final JwtProperties jwtProperties;

    @Override
    public TokenIssueResult issue(Long userId, RoleType roleType) {
        String accessToken = createTokenPort.createAccessToken(userId, roleType);
        String refreshToken = createTokenPort.createRefreshToken(userId, roleType);

        saveTokenPort.save(
                userId,
                refreshToken,
                jwtProperties.refreshToken().expiration()
        );

        return new TokenIssueResult(accessToken, refreshToken);
    }
}
