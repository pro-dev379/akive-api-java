package net.akive.api.auth.application.service.token;

import net.akive.api.auth.application.dto.result.token.IssueTokenResult;
import net.akive.api.auth.application.dto.result.token.ReissueTokenResult;
import net.akive.api.auth.application.port.out.token.CreateTokenPort;
import net.akive.api.auth.application.port.out.token.LoadTokenPort;
import net.akive.api.auth.application.port.out.token.RotateTokenPort;
import net.akive.api.auth.application.port.out.token.SaveTokenPort;
import net.akive.api.auth.application.port.out.token.ValidateTokenPort;
import net.akive.api.auth.domain.token.TokenPayload;
import net.akive.api.auth.domain.token.exception.TokenException;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.security.JwtProperties;
import net.akive.api.user.domain.RoleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static net.akive.api.auth.domain.token.exception.TokenErrorCode.EXPIRED_REFRESH_TOKEN;
import static net.akive.api.auth.domain.token.exception.TokenErrorCode.INVALID_REFRESH_TOKEN;
import static net.akive.api.user.domain.RoleType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    CreateTokenPort createTokenPort;

    @Mock
    LoadTokenPort loadTokenPort;

    @Mock
    SaveTokenPort saveTokenPort;

    @Mock
    RotateTokenPort rotateTokenPort;

    @Mock
    ValidateTokenPort validateTokenPort;

    @Mock
    CurrentTimeProvider currentTimeProvider;

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
        IssueTokenResult result = tokenService.issue(userId, roleType);

        // then
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);

        then(saveTokenPort).should().save(userId, refreshToken, refreshTokenExpiration);
    }

    @Test
    void 리프레시_토큰_재발행_시_만료까지_1분_이하로_남으면_리프레시_토큰을_원자적으로_교체한다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        Instant now = Instant.parse("2026-07-03T00:00:00Z");
        Instant expiresAt = now.plusSeconds(60);
        Duration refreshTokenExpiration = Duration.ofDays(14L);

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willReturn(new TokenPayload(userId, roleType, expiresAt));
        given(loadTokenPort.loadByUserId(userId))
                .willReturn(Optional.of(refreshToken));
        given(createTokenPort.createAccessToken(userId, roleType))
                .willReturn(newAccessToken);
        given(currentTimeProvider.instant())
                .willReturn(now);
        given(createTokenPort.createRefreshToken(userId, roleType))
                .willReturn(newRefreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);
        given(rotateTokenPort.rotate(userId, refreshToken, newRefreshToken, refreshTokenExpiration))
                .willReturn(true);

        // when
        ReissueTokenResult result = tokenService.reissue(refreshToken);

        // then
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isEqualTo(newRefreshToken);

        then(rotateTokenPort).should().rotate(userId, refreshToken, newRefreshToken, refreshTokenExpiration);
    }

    @Test
    void 리프레시_토큰_재발행_시_원자적_교체에_실패하면_유효하지_않은_리프레시_토큰_예외를_던진다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        Instant now = Instant.parse("2026-07-03T00:00:00Z");
        Instant expiresAt = now.plusSeconds(60);
        Duration refreshTokenExpiration = Duration.ofDays(14L);

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willReturn(new TokenPayload(userId, roleType, expiresAt));
        given(loadTokenPort.loadByUserId(userId))
                .willReturn(Optional.of(refreshToken));
        given(createTokenPort.createAccessToken(userId, roleType))
                .willReturn(newAccessToken);
        given(currentTimeProvider.instant())
                .willReturn(now);
        given(createTokenPort.createRefreshToken(userId, roleType))
                .willReturn(newRefreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);
        given(rotateTokenPort.rotate(userId, refreshToken, newRefreshToken, refreshTokenExpiration))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> tokenService.reissue(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰_재발행_시_만료까지_남은_시간이_0초이면_리프레시_토큰을_원자적으로_교체한다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        Instant now = Instant.parse("2026-07-03T00:00:00Z");
        Instant expiresAt = now;
        Duration refreshTokenExpiration = Duration.ofDays(14L);

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willReturn(new TokenPayload(userId, roleType, expiresAt));
        given(loadTokenPort.loadByUserId(userId))
                .willReturn(Optional.of(refreshToken));
        given(createTokenPort.createAccessToken(userId, roleType))
                .willReturn(newAccessToken);
        given(currentTimeProvider.instant())
                .willReturn(now);
        given(createTokenPort.createRefreshToken(userId, roleType))
                .willReturn(newRefreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);
        given(rotateTokenPort.rotate(userId, refreshToken, newRefreshToken, refreshTokenExpiration))
                .willReturn(true);

        // when
        ReissueTokenResult result = tokenService.reissue(refreshToken);

        // then
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isEqualTo(newRefreshToken);

        then(rotateTokenPort).should().rotate(userId, refreshToken, newRefreshToken, refreshTokenExpiration);
    }

    @Test
    void 리프레시_토큰_재발행_시_만료까지_1분보다_많이_남으면_액세스_토큰만_재발행한다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";
        Instant now = Instant.parse("2026-07-03T00:00:00Z");
        Instant expiresAt = now.plusSeconds(61);

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willReturn(new TokenPayload(userId, roleType, expiresAt));
        given(loadTokenPort.loadByUserId(userId))
                .willReturn(Optional.of(refreshToken));
        given(createTokenPort.createAccessToken(userId, roleType))
                .willReturn(newAccessToken);
        given(currentTimeProvider.instant())
                .willReturn(now);

        // when
        ReissueTokenResult result = tokenService.reissue(refreshToken);

        // then
        assertThat(result.accessToken()).isEqualTo(newAccessToken);
        assertThat(result.refreshToken()).isNull();

        verifyNoInteractions(rotateTokenPort);
    }

    @Test
    void 리프레시_토큰_재발행_시_저장된_리프레시_토큰이_없으면_유효하지_않은_리프레시_토큰_예외를_던진다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String refreshToken = "refresh-token";
        Instant expiresAt = Instant.parse("2026-07-03T00:01:00Z");

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willReturn(new TokenPayload(userId, roleType, expiresAt));
        given(loadTokenPort.loadByUserId(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tokenService.reissue(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);

        then(createTokenPort).should(never()).createAccessToken(userId, roleType);
        verifyNoInteractions(rotateTokenPort);
    }

    @Test
    void 리프레시_토큰_재발행_시_저장된_리프레시_토큰과_요청_토큰이_다르면_유효하지_않은_리프레시_토큰_예외를_던진다() {
        // given
        Long userId = 1L;
        RoleType roleType = USER;
        String refreshToken = "refresh-token";
        String savedRefreshToken = "saved-refresh-token";
        Instant expiresAt = Instant.parse("2026-07-03T00:01:00Z");

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willReturn(new TokenPayload(userId, roleType, expiresAt));
        given(loadTokenPort.loadByUserId(userId))
                .willReturn(Optional.of(savedRefreshToken));

        // when & then
        assertThatThrownBy(() -> tokenService.reissue(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);

        then(createTokenPort).should(never()).createAccessToken(userId, roleType);
        verifyNoInteractions(rotateTokenPort);
    }

    @Test
    void 리프레시_토큰_재발행_시_리프레시_토큰_검증에_실패하면_유효하지_않은_리프레시_토큰_예외를_던진다() {
        // given
        String refreshToken = "invalid-refresh-token";

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willThrow(new TokenException(INVALID_REFRESH_TOKEN));

        // when & then
        assertThatThrownBy(() -> tokenService.reissue(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_REFRESH_TOKEN);

        verifyNoInteractions(loadTokenPort);
        verifyNoInteractions(createTokenPort);
        verifyNoInteractions(rotateTokenPort);
    }

    @Test
    void 리프레시_토큰_재발행_시_만료된_리프레시_토큰이면_만료된_리프레시_토큰_예외를_던진다() {
        // given
        String refreshToken = "expired-refresh-token";

        given(validateTokenPort.validateRefreshToken(refreshToken))
                .willThrow(new TokenException(EXPIRED_REFRESH_TOKEN));

        // when & then
        assertThatThrownBy(() -> tokenService.reissue(refreshToken))
                .isInstanceOf(TokenException.class)
                .extracting("errorCode")
                .isEqualTo(EXPIRED_REFRESH_TOKEN);

        verifyNoInteractions(loadTokenPort);
        verifyNoInteractions(createTokenPort);
        verifyNoInteractions(rotateTokenPort);
    }
}
