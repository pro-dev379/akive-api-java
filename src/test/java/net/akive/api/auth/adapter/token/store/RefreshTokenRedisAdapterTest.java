package net.akive.api.auth.adapter.token.store;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.support.RedisTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class RefreshTokenRedisAdapterTest extends RedisTestSupport {

    private static final Duration TTL = Duration.ofDays(14);
    private static final Duration SHORT_TTL = Duration.ofMillis(300);

    @Test
    void 리프레시_토큰을_저장하고_조회한다() {
        RefreshToken refreshToken = createRefreshToken();
        ClientInstanceId clientInstanceId = createClientInstanceId();

        refreshTokenStore.save(USER_ID, clientInstanceId, refreshToken, TTL);

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(found).contains(refreshToken);
    }

    @Test
    void 리프레시_토큰을_삭제한다() {
        RefreshToken refreshToken = createRefreshToken();
        ClientInstanceId clientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, clientInstanceId, refreshToken, TTL);

        refreshTokenStore.delete(USER_ID, clientInstanceId);

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(found).isEmpty();
    }

    @Test
    void 저장된_리프레시_토큰은_TTL이_지나면_만료된다() {
        RefreshToken refreshToken = createRefreshToken();
        ClientInstanceId clientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, clientInstanceId, refreshToken, SHORT_TTL);

        await().atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    Optional<RefreshToken> found =
                            refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);
                    assertThat(found).isEmpty();
                });
    }

    @Test
    void 저장된_리프레시_토큰이_현재_토큰과_같으면_새_토큰으로_교체한다() {
        RefreshToken currentRefreshToken = createRefreshToken();
        RefreshToken newRefreshToken = createRefreshToken("new-refresh-token");
        ClientInstanceId clientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, clientInstanceId, currentRefreshToken, TTL);

        boolean rotated = refreshTokenStore.rotate(
                USER_ID,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                TTL
        );

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(rotated).isTrue();
        assertThat(found).contains(newRefreshToken);
    }

    @Test
    void 저장된_리프레시_토큰이_현재_토큰과_다르면_교체하지_않는다() {
        RefreshToken currentRefreshToken = createRefreshToken();
        RefreshToken otherRefreshToken = createRefreshToken("other-refresh-token");
        RefreshToken newRefreshToken = createRefreshToken("new-refresh-token");
        ClientInstanceId clientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, clientInstanceId, otherRefreshToken, TTL);

        boolean rotated = refreshTokenStore.rotate(
                USER_ID,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                TTL
        );

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(rotated).isFalse();
        assertThat(found).contains(otherRefreshToken);
    }

    @Test
    void 저장된_리프레시_토큰이_없으면_교체하지_않는다() {
        RefreshToken currentRefreshToken = createRefreshToken();
        RefreshToken newRefreshToken = createRefreshToken("new-refresh-token");
        ClientInstanceId clientInstanceId = createClientInstanceId();

        boolean rotated = refreshTokenStore.rotate(
                USER_ID,
                clientInstanceId,
                currentRefreshToken,
                newRefreshToken,
                TTL
        );

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(rotated).isFalse();
        assertThat(found).isEmpty();
    }
}
