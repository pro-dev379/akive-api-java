package net.akive.api.auth.adapter.token.store;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.token.required.RefreshTokenStore;
import net.akive.api.auth.domain.token.RefreshToken;
import net.akive.api.common.cache.CacheKeyProvider;
import net.akive.api.common.cache.RedisTemplateClient;
import net.akive.api.common.domain.ClientInstanceId;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class RefreshTokenRedisAdapter implements RefreshTokenStore {

    private final RedisTemplateClient redisTemplateClient;

    @Override
    public Optional<RefreshToken> findByUserIdAndClientInstantId(Long userId, ClientInstanceId clientInstanceId) {
        return redisTemplateClient.get(CacheKeyProvider.refreshToken(userId, clientInstanceId), RefreshToken.class);
    }

    @Override
    public void save(Long userId, ClientInstanceId clientInstanceId, RefreshToken refreshToken, Duration ttl) {
        redisTemplateClient.set(CacheKeyProvider.refreshToken(userId, clientInstanceId), refreshToken, ttl);
    }

    @Override
    public boolean rotate(
            Long userId,
            ClientInstanceId clientInstanceId,
            RefreshToken currentRefreshToken,
            RefreshToken newRefreshToken,
            Duration ttl
    ) {
        return redisTemplateClient.compareAndSet(
                CacheKeyProvider.refreshToken(userId, clientInstanceId),
                currentRefreshToken,
                newRefreshToken,
                ttl
        );
    }

    @Override
    public void delete(Long userId, ClientInstanceId clientInstanceId) {
        redisTemplateClient.delete(CacheKeyProvider.refreshToken(userId, clientInstanceId));
    }
}
