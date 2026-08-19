package net.akive.api.auth.adapter.token.security;

import net.akive.api.auth.domain.token.AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import static net.akive.api.auth.adapter.token.security.JwtProviderFixture.*;
import static net.akive.api.user.domain.RoleType.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenProviderTest {

    @Mock
    JwtEncoder accessTokenJwtEncoder;

    JwtAccessTokenProvider jwtAccessTokenProvider;

    @BeforeEach
    void setUp() {
        jwtAccessTokenProvider = new JwtAccessTokenProvider(
                accessTokenJwtEncoder,
                jwtProperties(),
                fixedCurrentTimeProvider()
        );
    }

    @Test
    void 액세스_토큰을_생성한다() {
        String tokenValue = "access-token";
        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .subject(String.valueOf(USER_ID))
                .build();

        given(accessTokenJwtEncoder.encode(any(JwtEncoderParameters.class))).willReturn(jwt);

        AccessToken accessToken = jwtAccessTokenProvider.create(USER_ID, MEMBER);

        assertThat(accessToken).isEqualTo(new AccessToken(tokenValue));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        then(accessTokenJwtEncoder).should().encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(claims.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.getIssuedAt()).isEqualTo(NOW);
        assertThat(claims.getExpiresAt())
                .isEqualTo(NOW.plusSeconds(ACCESS_TOKEN_EXPIRATION_MINUTES * 60));
        assertThat(claims.getClaimAsString("role")).isEqualTo(MEMBER.name());
    }
}
