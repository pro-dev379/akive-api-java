package net.akive.api.auth.adapter.token.security;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.required.RefreshTokenCreator;
import net.akive.api.auth.application.token.required.RefreshTokenParser;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;
import static net.akive.api.common.exception.business.AuthenticationError.REFRESH_TOKEN_EXPIRED;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

@Component
@RequiredArgsConstructor
class JwtRefreshTokenProvider implements RefreshTokenCreator, RefreshTokenParser {

    private final JwtEncoder refreshTokenJwtEncoder;
    private final JwtDecoder refreshTokenJwtDecoder;
    private final JwtProperties jwtProperties;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public RefreshToken create(Long userId, ClientInstanceId clientInstanceId, RoleType roleType) {
        Instant now = currentTimeProvider.instant();
        Instant expiresAt = now.plus(jwtProperties.refreshToken().expirationDays(), DAYS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("client", clientInstanceId.toString())
                .claim("role", roleType.name())
                .build();

        return new RefreshToken(encode(refreshTokenJwtEncoder, claims));
    }

    @Override
    public RefreshTokenPayload parse(RefreshToken refreshToken) throws AuthenticationException {
        Jwt jwt = decode(refreshToken);

        return createPayload(jwt);
    }

    private Jwt decode(RefreshToken refreshToken) {
        try {
            return refreshTokenJwtDecoder.decode(refreshToken.value());
        } catch (JwtValidationException e) {
            if (containsExpiredTokenError(e)) {
                throw new AuthenticationException(REFRESH_TOKEN_EXPIRED);
            }

            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        } catch (JwtException e) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }
    }

    private String encode(JwtEncoder jwtEncoder, JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }

    private RefreshTokenPayload createPayload(Jwt jwt) {
        try {
            return new RefreshTokenPayload(
                    Long.valueOf(jwt.getSubject()),
                    ClientInstanceId.parse(jwt.getClaimAsString("client")),
                    RoleType.valueOf(jwt.getClaimAsString("role")),
                    jwt.getExpiresAt()
            );
        } catch (RuntimeException e) {
            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }
    }

    private boolean containsExpiredTokenError(JwtValidationException e) {
        return e.getErrors()
                .stream()
                .map(OAuth2Error::getDescription)
                .anyMatch(description -> description != null && description.contains("expired"));
    }
}
