package net.akive.api.auth.infrastructure.token;

import net.akive.api.auth.domain.token.exception.TokenException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;
import java.time.Instant;

import static net.akive.api.auth.domain.token.exception.TokenErrorCode.EXPIRED_REFRESH_TOKEN;
import static net.akive.api.auth.domain.token.exception.TokenErrorCode.INVALID_REFRESH_TOKEN;
import static net.akive.api.auth.domain.token.TokenType.*;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    CurrentTimeProvider currentTimeProvider;

    JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        currentTimeProvider = new FixedCurrentTimeProvider(Instant.parse("2026-06-25T00:00:00Z"));

        jwtProperties = new JwtProperties(
                new JwtProperties.AccessTokenProperties("access-secret", 30),
                new JwtProperties.RefreshTokenProperties("refresh-secret", 14)
        );

        jwtTokenProvider = new JwtTokenProvider(
                accessTokenJwtEncoder,
                refreshTokenJwtEncoder,
                refreshTokenJwtDecoder,
                jwtProperties,
                currentTimeProvider
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

    @Test
    void 리프레시_토큰_검증_시_JWT_예외가_발생하면_유효하지_않은_리프레시_토큰_예외로_변환한다() {
        // given
        String refreshToken = "invalid-refresh-token";

        given(refreshTokenJwtDecoder.decode(refreshToken))
                .willThrow(new BadJwtException("Signed JWT rejected: Invalid signature"));

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateRefreshToken(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰_검증_시_만료_예외가_발생하면_만료된_리프레시_토큰_예외로_변환한다() {
        // given
        String refreshToken = "expired-refresh-token";
        OAuth2Error expiredError = new OAuth2Error("invalid_token", "Jwt expired at 2026-07-01T00:00:00Z", null);

        given(refreshTokenJwtDecoder.decode(refreshToken))
                .willThrow(new JwtValidationException("Jwt expired", List.of(expiredError)));

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateRefreshToken(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰을_검증한다() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";
        Instant expiresAt = Instant.parse("2026-07-09T00:00:00Z");

        Jwt jwt = Jwt.withTokenValue(refreshToken)
                .header("alg", "HS256")
                .subject(String.valueOf(userId))
                .claim("type", REFRESH.name())
                .claim("role", USER.name())
                .expiresAt(expiresAt)
                .build();

        given(refreshTokenJwtDecoder.decode(refreshToken))
                .willReturn(jwt);

        // when
        var payload = jwtTokenProvider.validateRefreshToken(refreshToken);

        // then
        assertThat(payload.userId()).isEqualTo(userId);
        assertThat(payload.roleType()).isEqualTo(USER);
        assertThat(payload.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void 리프레시_토큰_검증_시_토큰_타입이_리프레시가_아니면_유효하지_않은_리프레시_토큰_예외를_던진다() {
        // given
        String refreshToken = "refresh-token";

        Jwt jwt = Jwt.withTokenValue(refreshToken)
                .header("alg", "HS256")
                .subject("1")
                .claim("type", ACCESS.name())
                .claim("role", USER.name())
                .expiresAt(Instant.parse("2026-07-09T00:00:00Z"))
                .build();

        given(refreshTokenJwtDecoder.decode(refreshToken))
                .willReturn(jwt);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateRefreshToken(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰_검증_시_토큰_타입이_없으면_유효하지_않은_리프레시_토큰_예외를_던진다() {
        // given
        String refreshToken = "refresh-token";

        Jwt jwt = Jwt.withTokenValue(refreshToken)
                .header("alg", "HS256")
                .subject("1")
                .claim("role", USER.name())
                .expiresAt(Instant.parse("2026-07-09T00:00:00Z"))
                .build();

        given(refreshTokenJwtDecoder.decode(refreshToken))
                .willReturn(jwt);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateRefreshToken(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰_검증_시_회원_역할이_유효하지_않으면_유효하지_않은_리프레시_토큰_예외를_던진다() {
        // given
        String refreshToken = "refresh-token";

        Jwt jwt = Jwt.withTokenValue(refreshToken)
                .header("alg", "HS256")
                .subject("1")
                .claim("type", REFRESH.name())
                .claim("role", "INVALID")
                .expiresAt(Instant.parse("2026-07-09T00:00:00Z"))
                .build();

        given(refreshTokenJwtDecoder.decode(refreshToken))
                .willReturn(jwt);

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateRefreshToken(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }
}
