package net.akive.api.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class RedisTemplateClient {

    private static final long SCAN_COUNT = 1000L;
    private static final int BATCH_SIZE = 1000;

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

    private static final DefaultRedisScript<Long> COMPARE_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
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

    private <T> List<T> getValues(List<String> keys, Class<T> type) {
        List<Object> values = customRedisTemplate.opsForValue().multiGet(keys);

        if (values == null) return List.of();

        return values
                .stream()
                .filter(Objects::nonNull)
                .map(type::cast)
                .toList();
    }

    public <T> List<T> getByPattern(String pattern, Class<T> type) {
        List<T> values = new ArrayList<>();
        scanKeys(pattern, keys -> values.addAll(getValues(keys, type)));

        return values;
    }

    public void set(String key, Object value, Duration ttl) {
        customRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public boolean setIfAbsent(String key, Object value, Duration ttl) {
        return Boolean.TRUE.equals(customRedisTemplate.opsForValue().setIfAbsent(key, value, ttl));
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

    public boolean compareAndDelete(String key, Object expectedValue) {
        Long result = customRedisTemplate.execute(
                COMPARE_AND_DELETE_SCRIPT,
                List.of(key),
                expectedValue
        );

        return Long.valueOf(1L).equals(result);
    }

    public void delete(String key) {
        customRedisTemplate.delete(key);
    }

    public void deleteByPattern(String pattern) {
        scanKeys(pattern, customRedisTemplate::delete);
    }

    private void scanKeys(String pattern, Consumer<List<String>> batchConsumer) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(SCAN_COUNT)
                .build();

        List<String> keys = new ArrayList<>();

        try (Cursor<String> cursor = customRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());

                if (keys.size() >= BATCH_SIZE) {
                    batchConsumer.accept(List.copyOf(keys));
                    keys.clear();
                }
            }
        }

        if (!keys.isEmpty()) {
            batchConsumer.accept(List.copyOf(keys));
        }
    }
}
