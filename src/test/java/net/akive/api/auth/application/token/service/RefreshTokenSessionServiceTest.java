package net.akive.api.auth.application.token.service;

import net.akive.api.auth.application.token.required.RefreshTokenCreator;
import net.akive.api.auth.application.token.required.RefreshTokenStore;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.exception.business.UnauthorizedException;
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

import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.auth.domain.token.RefreshTokenPayloadFixture.createRefreshTokenPayload;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_REFRESH_TOKEN;
import static net.akive.api.user.domain.RoleType.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSessionServiceTest {

    @Mock
    RefreshTokenCreator refreshTokenCreator;

    @Mock
    RefreshTokenStore refreshTokenStore;

    @Mock
    RefreshTokenRotationPolicy refreshTokenRotationPolicy;

    @Mock
    JwtProperties jwtProperties;

    @Mock
    JwtProperties.RefreshTokenProperties refreshTokenProperties;

    @InjectMocks
    RefreshTokenSessionService refreshTokenSessionService;

    @Test
    void 리프레시_토큰_세션을_시작한다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RoleType roleType = MEMBER;
        RefreshToken refreshToken = createRefreshToken();
        Duration refreshTokenExpiration = Duration.ofDays(14);

        given(refreshTokenCreator.create(userId, clientInstanceId, roleType))
                .willReturn(refreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);

        RefreshToken result = refreshTokenSessionService.start(userId, clientInstanceId, roleType);

        assertThat(result).isEqualTo(refreshToken);

        then(refreshTokenCreator).should().create(userId, clientInstanceId, roleType);
        then(refreshTokenStore).should().save(userId, clientInstanceId, refreshToken, refreshTokenExpiration);
    }

    @Test
    void 리프레시_토큰_교체가_필요하면_새_리프레시_토큰으로_교체한다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RoleType roleType = MEMBER;
        Instant expiresAt = Instant.parse("2026-07-03T00:01:00Z");
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshToken newRefreshToken = createRefreshToken("new-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(userId, clientInstanceId, roleType, expiresAt);
        Duration refreshTokenExpiration = Duration.ofDays(14);

        given(refreshTokenStore.findByUserIdAndClientInstantId(userId, clientInstanceId))
                .willReturn(Optional.of(currentRefreshToken));
        given(refreshTokenRotationPolicy.requiresRotation(expiresAt))
                .willReturn(true);
        given(refreshTokenCreator.create(userId, clientInstanceId, roleType))
                .willReturn(newRefreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);
        given(refreshTokenStore.rotate(
                userId,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                refreshTokenExpiration
        )).willReturn(true);

        Optional<RefreshToken> result = refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken);

        assertThat(result).contains(newRefreshToken);

        then(refreshTokenStore).should().findByUserIdAndClientInstantId(userId, clientInstanceId);
        then(refreshTokenRotationPolicy).should().requiresRotation(expiresAt);
        then(refreshTokenCreator).should().create(userId, clientInstanceId, roleType);
        then(refreshTokenStore).should().rotate(
                userId,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                refreshTokenExpiration
        );
    }

    @Test
    void 리프레시_토큰_교체가_필요하지_않으면_교체하지_않는다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        Instant expiresAt = Instant.parse("2026-07-03T00:01:00Z");
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(userId, clientInstanceId, MEMBER, expiresAt);

        given(refreshTokenStore.findByUserIdAndClientInstantId(userId, clientInstanceId))
                .willReturn(Optional.of(currentRefreshToken));
        given(refreshTokenRotationPolicy.requiresRotation(expiresAt))
                .willReturn(false);

        Optional<RefreshToken> result = refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken);

        assertThat(result).isEmpty();

        then(refreshTokenStore).should().findByUserIdAndClientInstantId(userId, clientInstanceId);
        then(refreshTokenRotationPolicy).should().requiresRotation(expiresAt);
        then(refreshTokenCreator).shouldHaveNoInteractions();
        then(refreshTokenStore).should(never()).rotate(any(), any(), any(), any(), any());
    }

    @Test
    void 저장된_리프레시_토큰이_없으면_예외를_던진다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(
                userId,
                clientInstanceId,
                MEMBER,
                Instant.parse("2026-07-03T00:01:00Z")
        );

        given(refreshTokenStore.findByUserIdAndClientInstantId(userId, clientInstanceId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);

        then(refreshTokenRotationPolicy).shouldHaveNoInteractions();
        then(refreshTokenCreator).shouldHaveNoInteractions();
    }

    @Test
    void 저장된_리프레시_토큰과_현재_리프레시_토큰이_다르면_예외를_던진다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshToken savedRefreshToken = createRefreshToken("saved-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(
                userId,
                clientInstanceId,
                MEMBER,
                Instant.parse("2026-07-03T00:01:00Z")
        );

        given(refreshTokenStore.findByUserIdAndClientInstantId(userId, clientInstanceId))
                .willReturn(Optional.of(savedRefreshToken));

        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);

        then(refreshTokenRotationPolicy).shouldHaveNoInteractions();
        then(refreshTokenCreator).shouldHaveNoInteractions();
    }

    @Test
    void 리프레시_토큰_교체에_실패하면_예외를_던진다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RoleType roleType = MEMBER;
        Instant expiresAt = Instant.parse("2026-07-03T00:01:00Z");
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshToken newRefreshToken = createRefreshToken("new-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(userId, clientInstanceId, roleType, expiresAt);
        Duration refreshTokenExpiration = Duration.ofDays(14);

        given(refreshTokenStore.findByUserIdAndClientInstantId(userId, clientInstanceId))
                .willReturn(Optional.of(currentRefreshToken));
        given(refreshTokenRotationPolicy.requiresRotation(expiresAt))
                .willReturn(true);
        given(refreshTokenCreator.create(userId, clientInstanceId, roleType))
                .willReturn(newRefreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);
        given(refreshTokenStore.rotate(
                userId,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                refreshTokenExpiration
        )).willReturn(false);

        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken))
                .extracting(UnauthorizedException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    void 리프레시_토큰_세션을_삭제한다() {
        Long userId = 1L;
        ClientInstanceId clientInstanceId = createClientInstanceId();

        refreshTokenSessionService.revoke(userId, clientInstanceId);

        then(refreshTokenStore).should().delete(userId, clientInstanceId);
    }
}
