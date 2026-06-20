package net.akive.api.auth.infrastructure.token;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.token.RefreshTokenRepository;
import net.akive.api.common.cache.CacheKeyProvider;
import net.akive.api.common.cache.RedisTemplateClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements RefreshTokenRepository {

    private final RedisTemplateClient redisTemplateClient;

    @Override
    public Optional<String> findByUserId(Long userId) {
        return redisTemplateClient.get(CacheKeyProvider.refreshToken(userId), String.class);
    }

    @Override
    public void save(Long userId, String refreshToken, Duration ttl) {
        redisTemplateClient.set(CacheKeyProvider.refreshToken(userId), refreshToken, ttl);
    }

    @Override
    public void delete(Long userId) {
        redisTemplateClient.delete(CacheKeyProvider.refreshToken(userId));
    }
}
