package net.akive.api.auth.application.token.service;

import net.akive.api.auth.application.token.required.RefreshTokenCreator;
import net.akive.api.auth.application.token.required.RefreshTokenStore;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.auth.domain.token.RefreshTokenPayload;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.core.config.security.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static net.akive.api.auth.domain.token.RefreshTokenFixture.*;
import static net.akive.api.auth.domain.token.RefreshTokenPayloadFixture.createRefreshTokenPayload;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_REFRESH_TOKEN;
import static net.akive.api.user.domain.UserRole.MEMBER;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ROLE;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
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
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken refreshToken = createRefreshToken();

        given(refreshTokenCreator.create(USER_ID, clientInstanceId, USER_ROLE))
                .willReturn(refreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(REFRESH_TOKEN_EXPIRATION);

        RefreshToken result = refreshTokenSessionService.start(USER_ID, clientInstanceId, USER_ROLE);

        assertThat(result).isEqualTo(refreshToken);

        then(refreshTokenCreator).should().create(USER_ID, clientInstanceId, USER_ROLE);
        then(refreshTokenStore).should().save(USER_ID, clientInstanceId, refreshToken, REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    void 리프레시_토큰_교체가_필요하면_새_리프레시_토큰으로_교체한다() {
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshToken newRefreshToken = createRefreshToken("new-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(USER_ID, clientInstanceId, USER_ROLE, EXPIRE_AT);

        given(refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId))
                .willReturn(Optional.of(currentRefreshToken));
        given(refreshTokenRotationPolicy.requiresRotation(EXPIRE_AT))
                .willReturn(true);
        given(refreshTokenCreator.create(USER_ID, clientInstanceId, USER_ROLE))
                .willReturn(newRefreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(REFRESH_TOKEN_EXPIRATION);
        given(refreshTokenStore.rotate(
                USER_ID,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                REFRESH_TOKEN_EXPIRATION
        )).willReturn(true);

        Optional<RefreshToken> result = refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken);

        assertThat(result).contains(newRefreshToken);

        then(refreshTokenStore).should().findByUserIdAndClientInstantId(USER_ID, clientInstanceId);
        then(refreshTokenRotationPolicy).should().requiresRotation(EXPIRE_AT);
        then(refreshTokenCreator).should().create(USER_ID, clientInstanceId, USER_ROLE);
        then(refreshTokenStore).should().rotate(
                USER_ID,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                REFRESH_TOKEN_EXPIRATION
        );
    }

    @Test
    void 리프레시_토큰_교체가_필요하지_않으면_교체하지_않는다() {
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(USER_ID, clientInstanceId, MEMBER, EXPIRE_AT);

        given(refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId))
                .willReturn(Optional.of(currentRefreshToken));
        given(refreshTokenRotationPolicy.requiresRotation(EXPIRE_AT))
                .willReturn(false);

        Optional<RefreshToken> result = refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken);

        assertThat(result).isEmpty();

        then(refreshTokenStore).should().findByUserIdAndClientInstantId(USER_ID, clientInstanceId);
        then(refreshTokenRotationPolicy).should().requiresRotation(EXPIRE_AT);
        then(refreshTokenCreator).shouldHaveNoInteractions();
        then(refreshTokenStore).should(never()).rotate(any(), any(), any(), any(), any());
    }

    @Test
    void 저장된_리프레시_토큰이_없으면_예외를_던진다() {
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(
                USER_ID,
                clientInstanceId,
                MEMBER,
                EXPIRE_AT
        );

        given(refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId))
                .willReturn(Optional.empty());

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);

        then(refreshTokenRotationPolicy).shouldHaveNoInteractions();
        then(refreshTokenCreator).shouldHaveNoInteractions();
    }

    @Test
    void 저장된_리프레시_토큰과_현재_리프레시_토큰이_다르면_예외를_던진다() {
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshToken savedRefreshToken = createRefreshToken("saved-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(
                USER_ID,
                clientInstanceId,
                MEMBER,
                EXPIRE_AT
        );

        given(refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId))
                .willReturn(Optional.of(savedRefreshToken));

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);

        then(refreshTokenRotationPolicy).shouldHaveNoInteractions();
        then(refreshTokenCreator).shouldHaveNoInteractions();
    }

    @Test
    void 리프레시_토큰_교체에_실패하면_예외를_던진다() {
        ClientInstanceId clientInstanceId = createClientInstanceId();
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        RefreshToken newRefreshToken = createRefreshToken("new-refresh-token");
        RefreshTokenPayload payload = createRefreshTokenPayload(USER_ID, clientInstanceId, USER_ROLE, EXPIRE_AT);
        Duration refreshTokenExpiration = Duration.ofDays(14);

        given(refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId))
                .willReturn(Optional.of(currentRefreshToken));
        given(refreshTokenRotationPolicy.requiresRotation(EXPIRE_AT))
                .willReturn(true);
        given(refreshTokenCreator.create(USER_ID, clientInstanceId, USER_ROLE))
                .willReturn(newRefreshToken);
        given(jwtProperties.refreshToken())
                .willReturn(refreshTokenProperties);
        given(refreshTokenProperties.expiration())
                .willReturn(refreshTokenExpiration);
        given(refreshTokenStore.rotate(
                USER_ID,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                refreshTokenExpiration
        )).willReturn(false);

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> refreshTokenSessionService.rotateIfRequired(payload, currentRefreshToken))
                .extracting(AuthenticationException::getError)
                .isEqualTo(INVALID_REFRESH_TOKEN);
    }

    @Test
    void 회원_식별자로_리프레시_토큰_세션을_무효화한다() {
        refreshTokenSessionService.revokeByUserId(USER_ID);

        then(refreshTokenStore).should().deleteByUserId(USER_ID);
    }

    @Test
    void 회원_식별자와_클라이언트_식별자로_리프레시_토큰_세션을_무효화한다() {
        ClientInstanceId clientInstanceId = createClientInstanceId();

        refreshTokenSessionService.revokeByUserIdAndClientInstanceId(USER_ID, clientInstanceId);

        then(refreshTokenStore).should().deleteByUserIdAndClientInstanceId(USER_ID, clientInstanceId);
    }
}
