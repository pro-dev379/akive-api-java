package net.akive.api.coaching.adapter.store;

import net.akive.api.coaching.application.required.CoachingConversationLock;
import net.akive.api.coaching.application.required.CoachingResponseRecoveryStore;
import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;
import net.akive.api.support.RedisTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.createCoachingTurnRecoveryData;
import static net.akive.api.coaching.domain.fixture.CoachingConversationFixture.COACHING_CONVERSATION_ID;
import static net.akive.api.common.cache.CacheKeyProvider.coachingConversationLock;
import static net.akive.api.common.cache.CacheKeyProvider.pendingCoachingResponse;
import static org.assertj.core.api.Assertions.assertThat;

class CoachingConversationRedisAdapterTest extends RedisTestSupport {

    private static final long LOCK_TTL_SECONDS = Duration.ofMinutes(5).toSeconds();
    private static final long RESPONSE_TTL_SECONDS = Duration.ofDays(1).toSeconds();

    @Autowired
    private CoachingConversationLock conversationLock;

    @Autowired
    private CoachingResponseRecoveryStore responseRecoveryStore;

    @Test
    void 코칭_대화_락을_획득하고_해제한다() {
        CoachingConversationLock.Lease lease = conversationLock
                .tryAcquire(COACHING_CONVERSATION_ID)
                .orElseThrow();
        Long ttl = redisTemplate.getExpire(coachingConversationLock(COACHING_CONVERSATION_ID), SECONDS);

        assertThat(conversationLock.tryAcquire(COACHING_CONVERSATION_ID)).isEmpty();
        assertThat(ttl)
                .isPositive()
                .isLessThanOrEqualTo(LOCK_TTL_SECONDS);

        lease.release();

        assertThat(conversationLock.tryAcquire(COACHING_CONVERSATION_ID)).isPresent();
    }

    @Test
    void 이전_락_소유자는_현재_락_소유자의_코칭_대화_락을_해제할_수_없다() {
        CoachingConversationLock.Lease previousOwner = conversationLock
                .tryAcquire(COACHING_CONVERSATION_ID)
                .orElseThrow();
        String lockKey = coachingConversationLock(COACHING_CONVERSATION_ID);
        String currentOwnerToken = "current-owner-token";
        redisTemplate.opsForValue().set(lockKey, currentOwnerToken);

        previousOwner.release();

        assertThat(redisTemplate.opsForValue().get(lockKey)).isEqualTo(currentOwnerToken);
    }

    @Test
    void AI_코치_응답_메시지를_임시_보관하고_조회한다() {
        CoachingTurnRecoveryData recoveryData = createCoachingTurnRecoveryData();

        responseRecoveryStore.save(COACHING_CONVERSATION_ID, recoveryData);

        Optional<CoachingTurnRecoveryData> foundRecoveryData =
                responseRecoveryStore.find(COACHING_CONVERSATION_ID);
        Long ttl = redisTemplate.getExpire(
                pendingCoachingResponse(COACHING_CONVERSATION_ID),
                SECONDS
        );

        assertThat(foundRecoveryData).contains(recoveryData);
        assertThat(ttl)
                .isPositive()
                .isLessThanOrEqualTo(RESPONSE_TTL_SECONDS);
    }

    @Test
    void 임시_보관한_AI_코치_응답_메시지를_삭제한다() {
        responseRecoveryStore.save(
                COACHING_CONVERSATION_ID,
                createCoachingTurnRecoveryData()
        );

        responseRecoveryStore.delete(COACHING_CONVERSATION_ID);

        assertThat(responseRecoveryStore.find(COACHING_CONVERSATION_ID)).isEmpty();
    }
}
