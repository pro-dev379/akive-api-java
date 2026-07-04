package net.akive.api.auth.infrastructure.token;

import net.akive.api.support.StoreTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenRedisAdapterTest extends StoreTestSupport {

    @Test
    void 리프레시_토큰을_저장하고_조회한다() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        // when
        saveTokenPort.save(userId, refreshToken, Duration.ofMinutes(5));
        Optional<String> found = loadTokenPort.findByUserId(userId);

        // then
        assertThat(found).contains(refreshToken);
    }

    @Test
    void 리프레시_토큰을_삭제한다() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        saveTokenPort.save(userId, refreshToken, Duration.ofMinutes(5));

        // when
        deleteTokenPort.delete(userId);

        // then
        Optional<String> found = loadTokenPort.findByUserId(userId);

        assertThat(found).isEmpty();
    }

    @Test
    void 저장된_리프레시_토큰은_TTL이_지나면_만료된다() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        // when
        saveTokenPort.save(userId, refreshToken, Duration.ofMillis(300));

        // then
        await()
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    Optional<String> found = loadTokenPort.findByUserId(userId);

                    assertThat(found).isEmpty();
                });
    }

    @Test
    void 저장된_리프레시_토큰이_현재_토큰과_같으면_새_토큰으로_교체한다() {
        // given
        Long userId = 1L;
        String currentRefreshToken = "current-refresh-token";
        String newRefreshToken = "new-refresh-token";

        saveTokenPort.save(userId, currentRefreshToken, Duration.ofMinutes(5));

        // when
        boolean rotated = rotateTokenPort.rotate(
                userId,
                currentRefreshToken,
                newRefreshToken,
                Duration.ofMinutes(5)
        );

        // then
        Optional<String> found = loadTokenPort.findByUserId(userId);

        assertThat(rotated).isTrue();
        assertThat(found).contains(newRefreshToken);
    }

    @Test
    void 저장된_리프레시_토큰이_현재_토큰과_다르면_교체하지_않는다() {
        // given
        Long userId = 1L;
        String currentRefreshToken = "current-refresh-token";
        String otherRefreshToken = "other-refresh-token";
        String newRefreshToken = "new-refresh-token";

        saveTokenPort.save(userId, otherRefreshToken, Duration.ofMinutes(5));

        // when
        boolean rotated = rotateTokenPort.rotate(
                userId,
                currentRefreshToken,
                newRefreshToken,
                Duration.ofMinutes(5)
        );

        // then
        Optional<String> found = loadTokenPort.findByUserId(userId);

        assertThat(rotated).isFalse();
        assertThat(found).contains(otherRefreshToken);
    }

    @Test
    void 저장된_리프레시_토큰이_없으면_교체하지_않는다() {
        // given
        Long userId = 1L;
        String currentRefreshToken = "current-refresh-token";
        String newRefreshToken = "new-refresh-token";

        // when
        boolean rotated = rotateTokenPort.rotate(
                userId,
                currentRefreshToken,
                newRefreshToken,
                Duration.ofMinutes(5)
        );

        // then
        Optional<String> found = loadTokenPort.findByUserId(userId);

        assertThat(rotated).isFalse();
        assertThat(found).isEmpty();
    }
}
