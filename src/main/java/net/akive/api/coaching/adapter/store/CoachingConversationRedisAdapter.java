package net.akive.api.coaching.adapter.store;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.coaching.application.required.CoachingConversationLock;
import net.akive.api.coaching.application.required.CoachingResponseRecoveryStore;
import net.akive.api.coaching.application.required.data.CoachingTurnRecoveryData;
import net.akive.api.common.cache.RedisTemplateClient;
import net.akive.api.common.id.UuidGenerator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

import static net.akive.api.common.cache.CacheKeyProvider.coachingConversationLock;
import static net.akive.api.common.cache.CacheKeyProvider.pendingCoachingResponse;

@Slf4j
@Component
@RequiredArgsConstructor
class CoachingConversationRedisAdapter implements CoachingConversationLock, CoachingResponseRecoveryStore {

    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final Duration PENDING_RESPONSE_TTL = Duration.ofDays(1);

    private final RedisTemplateClient redisTemplateClient;
    private final UuidGenerator uuidGenerator;

    @Override
    public Optional<Lease> tryAcquire(Long coachingConversationId) {
        String key = coachingConversationLock(coachingConversationId);
        String token = uuidGenerator.generate().toString();

        if (!redisTemplateClient.setIfAbsent(key, token, LOCK_TTL)) {
            return Optional.empty();
        }

        return Optional.of(() -> release(key, token));
    }

    @Override
    public Optional<CoachingTurnRecoveryData> find(Long coachingConversationId) {
        return redisTemplateClient.get(
                pendingCoachingResponse(coachingConversationId),
                CoachingTurnRecoveryData.class
        );
    }

    @Override
    public void save(Long coachingConversationId, CoachingTurnRecoveryData recoveryData) {
        redisTemplateClient.set(
                pendingCoachingResponse(coachingConversationId),
                recoveryData,
                PENDING_RESPONSE_TTL
        );
    }

    @Override
    public void delete(Long coachingConversationId) {
        redisTemplateClient.delete(pendingCoachingResponse(coachingConversationId));
    }

    private void release(String key, String token) {
        try {
            redisTemplateClient.compareAndDelete(key, token);
        } catch (RuntimeException e) {
            log.error("Failed to release coaching conversation lock: key={}", key, e);
        }
    }
}
