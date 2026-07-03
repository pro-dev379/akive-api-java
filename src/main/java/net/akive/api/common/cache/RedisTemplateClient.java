package net.akive.api.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisTemplateClient {

    private static final DefaultRedisScript<Long> COMPARE_AND_SET_SCRIPT = new DefaultRedisScript<>(
            """
            local ttl = tonumber(cjson.decode(ARGV[3]))
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                redis.call('SET', KEYS[1], ARGV[2], 'PX', ttl)
                return 1
            end
            return 0
            """,
            Long.class
    );

    private final RedisTemplate<String, Object> customRedisTemplate;

    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = customRedisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();
        return Optional.of(type.cast(value));
    }

    public void set(String key, Object value, Duration ttl) {
        customRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public boolean compareAndSet(String key, Object expectedValue, Object newValue, Duration ttl) {
        Long result = customRedisTemplate.execute(
                COMPARE_AND_SET_SCRIPT,
                List.of(key),
                expectedValue,
                newValue,
                String.valueOf(ttl.toMillis())
        );

        return Long.valueOf(1L).equals(result);
    }

    public void delete(String key) {
        customRedisTemplate.delete(key);
    }
}
