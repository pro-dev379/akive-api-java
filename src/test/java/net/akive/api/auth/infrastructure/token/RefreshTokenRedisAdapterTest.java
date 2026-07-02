package net.akive.api.auth.infrastructure.token;

import net.akive.api.support.StoreTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

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
    void 저장된_리프레시_토큰은_TTL이_지나면_만료된다() throws Exception {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        // when
        saveTokenPort.save(userId, refreshToken, Duration.ofMillis(300));

        Thread.sleep(500);

        Optional<String> found = loadTokenPort.findByUserId(userId);

        // then
        assertThat(found).isEmpty();
    }
}