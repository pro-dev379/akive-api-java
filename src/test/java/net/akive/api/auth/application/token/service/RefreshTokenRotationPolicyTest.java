package net.akive.api.auth.application.token.service;

import net.akive.api.common.time.CurrentTimeProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static net.akive.api.auth.adapter.token.fixture.JwtProviderFixture.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRotationPolicyTest {

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    RefreshTokenRotationPolicy refreshTokenRotationPolicy;

    @ParameterizedTest
    @ValueSource(longs = {1, 60})
    void 리프레시_토큰의_남은_유효시간이_60초_이하이면_교체가_필요하다(long remainingSeconds) {
        Instant expiresAt = NOW.plusSeconds(remainingSeconds);
        given(currentTimeProvider.instant()).willReturn(NOW);

        boolean result = refreshTokenRotationPolicy.requiresRotation(expiresAt);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 61})
    void 리프레시_토큰이_만료됐거나_남은_유효시간이_60초를_초과하면_교체가_필요하지_않다(long remainingSeconds) {
        Instant expiresAt = NOW.plusSeconds(remainingSeconds);
        given(currentTimeProvider.instant()).willReturn(NOW);

        boolean result = refreshTokenRotationPolicy.requiresRotation(expiresAt);

        assertThat(result).isFalse();
    }
}