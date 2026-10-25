package net.akive.api.auth.adapter.token.store;

import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.support.RedisTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static net.akive.api.auth.domain.token.RefreshTokenFixture.createRefreshToken;
import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.fixture.UserFixture.ANOTHER_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class RefreshTokenRedisAdapterTest extends RedisTestSupport {

    private static final Duration TTL = Duration.ofDays(14);
    private static final Duration SHORT_TTL = Duration.ofMillis(300);

    @Test
    void 회원_식별자와_클라이언트_식별자로_리프레시_토큰을_저장하고_조회한다() {
        RefreshToken refreshToken = createRefreshToken();
        ClientInstanceId clientInstanceId = createClientInstanceId();

        refreshTokenStore.save(USER_ID, clientInstanceId, refreshToken, TTL);

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(found).contains(refreshToken);
    }

    @Test
    void 회원_식별자로_해당_회원의_모든_리프레시_토큰을_조회한다() {
        RefreshToken firstRefreshToken = createRefreshToken("first-refresh-token");
        RefreshToken secondRefreshToken = createRefreshToken("second-refresh-token");
        RefreshToken anotherUserRefreshToken = createRefreshToken("another-user-refresh-token");
        ClientInstanceId firstClientInstanceId = createClientInstanceId();
        ClientInstanceId secondClientInstanceId = createClientInstanceId();
        ClientInstanceId anotherUserClientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, firstClientInstanceId, firstRefreshToken, TTL);
        refreshTokenStore.save(USER_ID, secondClientInstanceId, secondRefreshToken, TTL);
        refreshTokenStore.save(ANOTHER_USER_ID, anotherUserClientInstanceId, anotherUserRefreshToken, TTL);

        List<RefreshToken> userRefreshTokens = refreshTokenStore.findByUserId(USER_ID);


        assertThat(userRefreshTokens).containsExactlyInAnyOrder(firstRefreshToken, secondRefreshToken);
    }

    @Test
    void 회원_식별자로_해당_회원의_모든_리프레시_토큰을_삭제한다() {
        RefreshToken firstRefreshToken = createRefreshToken("first-refresh-token");
        RefreshToken secondRefreshToken = createRefreshToken("second-refresh-token");
        RefreshToken anotherUserRefreshToken = createRefreshToken("another-user-refresh-token");
        ClientInstanceId firstClientInstanceId = createClientInstanceId();
        ClientInstanceId secondClientInstanceId = createClientInstanceId();
        ClientInstanceId anotherUserClientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, firstClientInstanceId, firstRefreshToken, TTL);
        refreshTokenStore.save(USER_ID, secondClientInstanceId, secondRefreshToken, TTL);
        refreshTokenStore.save(ANOTHER_USER_ID, anotherUserClientInstanceId, anotherUserRefreshToken, TTL);

        refreshTokenStore.deleteByUserId(USER_ID);

        List<RefreshToken> userRefreshTokens = refreshTokenStore.findByUserId(USER_ID);
        List<RefreshToken> anotherUserRefreshTokens = refreshTokenStore.findByUserId(ANOTHER_USER_ID);

        assertThat(userRefreshTokens).isEmpty();
        assertThat(anotherUserRefreshTokens).contains(anotherUserRefreshToken);
    }

    @Test
    void 저장된_리프레시_토큰이_현재_토큰과_같으면_삭제한다() {
        RefreshToken refreshToken = createRefreshToken();
        ClientInstanceId clientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, clientInstanceId, refreshToken, TTL);

        boolean deleted = refreshTokenStore.deleteIfMatches(USER_ID, clientInstanceId, refreshToken);

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(deleted).isTrue();
        assertThat(found).isEmpty();
    }

    @Test
    void 저장된_리프레시_토큰이_현재_토큰과_다르면_삭제하지_않는다() {
        RefreshToken savedRefreshToken = createRefreshToken("saved-refresh-token");
        RefreshToken currentRefreshToken = createRefreshToken("current-refresh-token");
        ClientInstanceId clientInstanceId = createClientInstanceId();
        refreshTokenStore.save(USER_ID, clientInstanceId, savedRefreshToken, TTL);

        boolean deleted = refreshTokenStore.deleteIfMatches(USER_ID, clientInstanceId, currentRefreshToken);

        Optional<RefreshToken> found = refreshTokenStore.findByUserIdAndClientInstantId(USER_ID, clientInstanceId);

        assertThat(deleted).isFalse();
        assertThat(found).contains(savedRefreshToken);
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
