package net.akive.api.auth.adapter.token.security;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.time.FixedCurrentTimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;

import static net.akive.api.auth.adapter.token.fixture.JwtProviderFixture.*;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;
import static net.akive.api.common.exception.business.AuthenticationError.REFRESH_TOKEN_EXPIRED;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ROLE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JwtRefreshTokenProviderTest {

    @Mock
    JwtEncoder refreshTokenJwtEncoder;

    @Mock
    JwtDecoder refreshTokenJwtDecoder;

    JwtRefreshTokenProvider jwtRefreshTokenProvider;

    @BeforeEach
    void setUp() {
        jwtRefreshTokenProvider = new JwtRefreshTokenProvider(
                refreshTokenJwtEncoder,
                refreshTokenJwtDecoder,
                jwtProperties(),
                new FixedCurrentTimeProvider(NOW)
        );
    }

    @Test
    void 리프레시_토큰을_생성한다() {
        Jwt jwt = Jwt.withTokenValue(REFRESH_TOKEN_VALUE)
                .header("alg", "HS256")
                .subject(String.valueOf(USER_ID))
                .build();

        given(refreshTokenJwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(jwt);

        RefreshToken refreshToken = jwtRefreshTokenProvider.create(USER_ID, CLIENT_INSTANCE_ID, USER_ROLE);

        assertThat(refreshToken).isEqualTo(new RefreshToken(REFRESH_TOKEN_VALUE));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        then(refreshTokenJwtEncoder).should().encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(claims.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.getIssuedAt()).isEqualTo(NOW);
        assertThat(claims.getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(claims.getClaimAsString("client")).isEqualTo(CLIENT_INSTANCE_ID.toString());
        assertThat(claims.getClaimAsString("role")).isEqualTo(USER_ROLE.name());
    }

    @Test
    void 리프레시_토큰을_파싱해_페이로드를_반환한다() {
        RefreshToken refreshToken = createRefreshToken();
        Jwt jwt = Jwt.withTokenValue(refreshToken.value())
                .header("alg", "HS256")
                .subject(String.valueOf(USER_ID))
                .claim("client", CLIENT_INSTANCE_ID.toString())
                .claim("role", USER_ROLE.name())
                .expiresAt(EXPIRES_AT)
                .build();

        given(refreshTokenJwtDecoder.decode(refreshToken.value())).willReturn(jwt);

        RefreshTokenPayload payload = jwtRefreshTokenProvider.parse(refreshToken);

        assertThat(payload.userId()).isEqualTo(USER_ID);
        assertThat(payload.clientInstanceId()).isEqualTo(CLIENT_INSTANCE_ID);
        assertThat(payload.role()).isEqualTo(USER_ROLE);
        assertThat(payload.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @Test
    void 리프레시_토큰_파싱_시_JWT_예외가_발생하면_유효하지_않은_리프레시_토큰_예외로_변환해서_던진다() {
        RefreshToken refreshToken = createRefreshToken("invalid-refresh-token");

        given(refreshTokenJwtDecoder.decode(refreshToken.value()))
                .willThrow(new BadJwtException("Signed JWT rejected: Invalid signature"));

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> jwtRefreshTokenProvider.parse(refreshToken))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰_파싱_시_JWT_만료_예외가_발생하면_만료된_리프레시_토큰_예외로_변환해서_던진다() {
        RefreshToken refreshToken = createRefreshToken("expired-refresh-token");
        OAuth2Error expiredError = new OAuth2Error(
                "invalid_token",
                "Jwt expired at 2026-08-15T12:00:00Z",
                null
        );

        given(refreshTokenJwtDecoder.decode(refreshToken.value()))
                .willThrow(new JwtValidationException("Jwt expired", List.of(expiredError)));

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> jwtRefreshTokenProvider.parse(refreshToken))
                .extracting(AuthenticationException::getError)
                .isEqualTo(REFRESH_TOKEN_EXPIRED);
    }
}
