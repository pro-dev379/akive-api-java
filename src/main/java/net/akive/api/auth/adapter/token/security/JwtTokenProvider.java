package net.akive.api.auth.adapter.token.security;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.required.CreateTokenPort;
import net.akive.api.auth.domain.token.TokenPayload;
import net.akive.api.auth.application.token.required.ValidateTokenPort;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static net.akive.api.auth.domain.token.TokenType.*;
import static net.akive.api.common.exception.business.UnauthorizedError.REFRESH_TOKEN_EXPIRED;
import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_REFRESH_TOKEN;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

@Component
@RequiredArgsConstructor
class JwtTokenProvider implements CreateTokenPort, ValidateTokenPort {

    private static final String ISSUER = "akive";

    private final JwtEncoder accessTokenJwtEncoder;
    private final JwtEncoder refreshTokenJwtEncoder;
    private final JwtDecoder refreshTokenJwtDecoder;
    private final JwtProperties jwtProperties;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public String createAccessToken(Long userId, RoleType roleType) {
        Instant now = currentTimeProvider.instant();
        Instant expiresAt = now.plus(jwtProperties.accessToken().expirationMinutes(), MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("type", ACCESS.name())
                .claim("role", roleType.name())
                .build();

        return encode(accessTokenJwtEncoder, claims);
    }

    @Override
    public String createRefreshToken(Long userId, RoleType roleType) {
        Instant now = currentTimeProvider.instant();
        Instant expiresAt = now.plus(jwtProperties.refreshToken().expirationDays(), DAYS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("type", REFRESH.name())
                .claim("role", roleType.name())
                .build();

        return encode(refreshTokenJwtEncoder, claims);
    }

    @Override
    public TokenPayload validateRefreshToken(String refreshToken) {
        Jwt jwt = decodeRefreshToken(refreshToken);

        validateRefreshTokenType(jwt);

        return createTokenPayload(jwt);
    }

    private Jwt decodeRefreshToken(String refreshToken) {
        try {
            return refreshTokenJwtDecoder.decode(refreshToken);
        } catch (JwtValidationException e) {
            if (containsExpiredTokenError(e)) {
                throw new UnauthorizedException(REFRESH_TOKEN_EXPIRED);
            }

            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        } catch (JwtException e) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
    }

    private void validateRefreshTokenType(Jwt jwt) {
        if (!REFRESH.name().equals(jwt.getClaimAsString("type"))) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
    }

    private TokenPayload createTokenPayload(Jwt jwt) {
        try {
            return new TokenPayload(
                    Long.valueOf(jwt.getSubject()),
                    RoleType.valueOf(jwt.getClaimAsString("role")),
                    jwt.getExpiresAt()
            );
        } catch (RuntimeException e) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
    }

    private boolean containsExpiredTokenError(JwtValidationException e) {
        return e.getErrors()
                .stream()
                .map(OAuth2Error::getDescription)
                .anyMatch(description -> description != null && description.contains("expired"));
    }

    private String encode(JwtEncoder jwtEncoder, JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
