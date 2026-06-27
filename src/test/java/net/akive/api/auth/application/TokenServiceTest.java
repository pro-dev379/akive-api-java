package net.akive.api.auth.application;

import net.akive.api.auth.application.dto.result.TokenIssueResult;
import net.akive.api.auth.application.port.out.token.CreateTokenPort;
import net.akive.api.auth.application.port.out.token.SaveTokenPort;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static net.akive.api.user.domain.RoleType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    CreateTokenPort createTokenPort;

    @Mock
    SaveTokenPort saveTokenPort;

    @Mock
    JwtProperties jwtProperties;

    @Mock
    JwtProperties.RefreshTokenProperties refreshTokenProperties;

    @InjectMocks
    TokenService tokenService;

    @Test
    void 유저의_토큰을_발급하고_리프레시_토큰을_저장한다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        Duration refreshTokenExpiration = Duration.ofDays(14L);

        given(createTokenPort.createAccessToken(userId, roleType))
                .willReturn(accessToken);
        given(createTokenPort.createRefreshToken(userId, roleType))
                .willReturn(refreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);

        // when
        TokenIssueResult result = tokenService.issue(userId, roleType);

        // then
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);

        then(saveTokenPort).should().save(userId, refreshToken, refreshTokenExpiration);
    }
}