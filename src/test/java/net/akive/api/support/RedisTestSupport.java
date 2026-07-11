package net.akive.api.support;

import net.akive.api.auth.application.port.out.emailverification.DeleteEmailVerificationPort;
import net.akive.api.auth.application.port.out.emailverification.LoadEmailVerificationPort;
import net.akive.api.auth.application.port.out.emailverification.SaveEmailVerificationPort;
import net.akive.api.auth.application.port.out.token.DeleteTokenPort;
import net.akive.api.auth.application.port.out.token.LoadTokenPort;
import net.akive.api.auth.application.port.out.token.RotateTokenPort;
import net.akive.api.auth.application.port.out.token.SaveTokenPort;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

@Testcontainers
public class RedisTestSupport extends IntegrationTestSupport{

    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2").withExposedPorts(6379);

    static {
        redis.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    protected LoadEmailVerificationPort loadEmailVerificationPort;

    @Autowired
    protected SaveEmailVerificationPort saveEmailVerificationPort;

    @Autowired
    protected DeleteEmailVerificationPort deleteEmailVerificationPort;

    @Autowired
    protected LoadTokenPort loadTokenPort;

    @Autowired
    protected SaveTokenPort saveTokenPort;

    @Autowired
    protected RotateTokenPort rotateTokenPort;

    @Autowired
    protected DeleteTokenPort deleteTokenPort;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(redisTemplate.getConnectionFactory());

        try (var connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }
    }
}
