package net.akive.api.auth.adapter.token.security;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.required.AccessTokenCreator;
import net.akive.api.auth.domain.token.AccessToken;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.UserRole;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

@Component
@RequiredArgsConstructor
class JwtAccessTokenProvider implements AccessTokenCreator {

    private final JwtEncoder accessTokenJwtEncoder;
    private final JwtProperties jwtProperties;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public AccessToken create(Long userId, UserRole role) {
        Instant now = currentTimeProvider.now();
        Instant expiresAt = now.plus(jwtProperties.accessToken().expirationMinutes(), MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("role", role.name())
                .build();

        return new AccessToken(encode(accessTokenJwtEncoder, claims));
    }

    private String encode(JwtEncoder jwtEncoder, JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
