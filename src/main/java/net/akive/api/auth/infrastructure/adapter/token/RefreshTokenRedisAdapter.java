package net.akive.api.auth.infrastructure.adapter.token;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.token.DeleteTokenPort;
import net.akive.api.auth.application.port.out.token.LoadTokenPort;
import net.akive.api.auth.application.port.out.token.RotateTokenPort;
import net.akive.api.auth.application.port.out.token.SaveTokenPort;
import net.akive.api.common.cache.CacheKeyProvider;
import net.akive.api.common.cache.RedisTemplateClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class RefreshTokenRedisAdapter implements LoadTokenPort, SaveTokenPort, RotateTokenPort, DeleteTokenPort {

    private final RedisTemplateClient redisTemplateClient;

    @Override
    public Optional<String> loadByUserId(Long userId) {
        return redisTemplateClient.get(CacheKeyProvider.refreshToken(userId), String.class);
    }

    @Override
    public void save(Long userId, String refreshToken, Duration ttl) {
        redisTemplateClient.set(CacheKeyProvider.refreshToken(userId), refreshToken, ttl);
    }

    @Override
    public boolean rotate(Long userId, String currentRefreshToken, String newRefreshToken, Duration ttl) {
        return redisTemplateClient.compareAndSet(
                CacheKeyProvider.refreshToken(userId),
                currentRefreshToken,
                newRefreshToken,
                ttl
        );
    }

    @Override
    public void delete(Long userId) {
        redisTemplateClient.delete(CacheKeyProvider.refreshToken(userId));
    }
}
