package net.akive.api.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResponseCache {

    private final RedisTemplateClient redisTemplateClient;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> get(String key, Class<T> type) {
        return redisTemplateClient.get(key, String.class)
                .map(json -> deserialize(json, type));
    }

    public void put(String key, Object value, Duration ttl) {
        redisTemplateClient.set(key, serialize(value), ttl);
    }

    public void evictByPattern(String pattern) {
        redisTemplateClient.deleteByPattern(pattern);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize cached response", e);
        }
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize cached response", e);
        }
    }
}
