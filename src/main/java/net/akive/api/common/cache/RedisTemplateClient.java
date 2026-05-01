package net.akive.api.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisTemplateClient {

    private final RedisTemplate<String, Object> customRedisTemplate;

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = customRedisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();
        return Optional.of(type.cast(value));
    }

    public void set(String key, Object value, Duration ttl) {
        customRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public void delete(String key) {
        customRedisTemplate.delete(key);
    }
}
