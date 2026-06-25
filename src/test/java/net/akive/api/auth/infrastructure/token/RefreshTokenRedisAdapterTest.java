package net.akive.api.auth.infrastructure.token;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class RefreshTokenRedisAdapterTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    RefreshTokenRedisAdapter refreshTokenRedisAdapter;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        RedisConnectionFactory connectionFactory =
                Objects.requireNonNull(redisTemplate.getConnectionFactory());

        connectionFactory.getConnection()
                .serverCommands()
                .flushDb();
    }

    @Test
    void 리프레시_토큰을_저장하고_조회한다() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        // when
        refreshTokenRedisAdapter.save(userId, refreshToken, Duration.ofMinutes(5));
        Optional<String> found = refreshTokenRedisAdapter.findByUserId(userId);

        // then
        assertThat(found).contains(refreshToken);
    }

    @Test
    void 리프레시_토큰을_삭제한다() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        refreshTokenRedisAdapter.save(userId, refreshToken, Duration.ofMinutes(5));

        // when
        refreshTokenRedisAdapter.delete(userId);

        // then
        Optional<String> found = refreshTokenRedisAdapter.findByUserId(userId);

        assertThat(found).isEmpty();
    }

    @Test
    void 저장된_리프레시_토큰은_TTL이_지나면_만료된다() throws Exception {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token";

        // when
        refreshTokenRedisAdapter.save(userId, refreshToken, Duration.ofMillis(300));

        Thread.sleep(500);

        Optional<String> found = refreshTokenRedisAdapter.findByUserId(userId);

        // then
        assertThat(found).isEmpty();
    }
}