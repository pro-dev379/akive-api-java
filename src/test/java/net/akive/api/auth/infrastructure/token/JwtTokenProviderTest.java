package net.akive.api.auth.infrastructure.token;

import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static net.akive.api.auth.domain.token.TokenType.*;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    JwtEncoder accessTokenJwtEncoder;

    @Mock
    JwtEncoder refreshTokenJwtEncoder;

    @Mock
    JwtDecoder refreshTokenJwtDecoder;

    JwtTokenProvider jwtTokenProvider;

    Clock clock;

    JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2026-06-25T00:00:00Z"),
                ZoneOffset.UTC
        );

        jwtProperties = new JwtProperties(
                new JwtProperties.AccessTokenProperties("access-secret", 30),
                new JwtProperties.RefreshTokenProperties("refresh-secret", 14)
        );

        jwtTokenProvider = new JwtTokenProvider(
                accessTokenJwtEncoder,
                refreshTokenJwtEncoder,
                refreshTokenJwtDecoder,
                jwtProperties,
                clock
        );
    }

    @Test
    void 액세스_토큰을_생성한다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String tokenValue = "access-token";

        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .claim("sub", String.valueOf(userId))
                .build();

        given(accessTokenJwtEncoder.encode(any(JwtEncoderParameters.class)))
                .willReturn(jwt);

        // when
        String accessToken = jwtTokenProvider.createAccessToken(userId, roleType);

        // then
        assertThat(accessToken).isEqualTo(tokenValue);

        ArgumentCaptor<JwtEncoderParameters> captor
                = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        then(accessTokenJwtEncoder).should()
                .encode(captor.capture());

        JwtEncoderParameters parameters = captor.getValue();
        JwtClaimsSet claims = parameters.getClaims();

        assertThat(claims.getClaimAsString("iss")).isEqualTo("akive");
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.getIssuedAt()).isEqualTo(Instant.parse("2026-06-25T00:00:00Z"));
        assertThat(claims.getExpiresAt()).isEqualTo(Instant.parse("2026-06-25T00:30:00Z"));
        assertThat(claims.getClaimAsString("type")).isEqualTo(ACCESS.name());
        assertThat(claims.getClaimAsString("role")).isEqualTo(USER.name());
    }

    @Test
    void 리프레시_토큰을_생성한다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String tokenValue = "refresh-token";

        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .claim("sub", String.valueOf(userId))
                .build();

        given(refreshTokenJwtEncoder.encode(any(JwtEncoderParameters.class)))
                .willReturn(jwt);

        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, roleType);

        // then
        assertThat(refreshToken).isEqualTo(tokenValue);

        ArgumentCaptor<JwtEncoderParameters> captor
                = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        then(refreshTokenJwtEncoder).should()
                .encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();

        assertThat(claims.getClaimAsString("iss")).isEqualTo("akive");
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.getIssuedAt()).isEqualTo(Instant.parse("2026-06-25T00:00:00Z"));
        assertThat(claims.getExpiresAt()).isEqualTo(Instant.parse("2026-07-09T00:00:00Z"));
        assertThat(claims.getClaimAsString("type")).isEqualTo(REFRESH.name());
        assertThat(claims.getClaimAsString("role")).isEqualTo(USER.name());
    }
}