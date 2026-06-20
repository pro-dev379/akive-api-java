package net.akive.api.auth.infrastructure.token;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.token.TokenGenerator;
import net.akive.api.auth.domain.token.TokenPayload;
import net.akive.api.auth.application.port.out.token.TokenValidator;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenGenerator, TokenValidator {

    private static final String ISSUER = "akive";

    private final JwtEncoder accessTokenJwtEncoder;
    private final JwtEncoder refreshTokenJwtEncoder;
    private final JwtDecoder refreshTokenJwtDecoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Override
    public String createAccessToken(Long userId, RoleType roleType) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(jwtProperties.accessToken().expirationMinutes(), MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("type", "access")
                .claim("role", roleType.name())
                .build();

        return encode(accessTokenJwtEncoder, claims);
    }

    @Override
    public String createRefreshToken(Long userId, RoleType roleType) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(jwtProperties.refreshToken().expirationDays(), DAYS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("type", "refresh")
                .claim("role", roleType.name())
                .build();

        return encode(refreshTokenJwtEncoder, claims);
    }

    @Override
    public TokenPayload validateRefreshToken(String refreshToken) {
        Jwt jwt = refreshTokenJwtDecoder.decode(refreshToken);

        return new TokenPayload(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaim("role")
        );
    }

    private String encode(JwtEncoder jwtEncoder, JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
