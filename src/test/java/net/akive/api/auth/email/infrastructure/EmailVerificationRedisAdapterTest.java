package net.akive.api.auth.email.infrastructure;

import net.akive.api.auth.email.domain.EmailVerification;
import net.akive.api.auth.email.domain.EmailVerificationPurpose;
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
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class EmailVerificationRedisAdapterTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    EmailVerificationRedisAdapter emailAuthRedisAdapter;

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
    void 이메일_인증_정보를_저장하고_조회한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        EmailVerification emailVerification = EmailVerification.create(email, verificationPurpose, "123456", requestedAt);

        // when
        emailAuthRedisAdapter.save(emailVerification, Duration.ofMinutes(5));
        Optional<EmailVerification> found = emailAuthRedisAdapter.findByEmailAndVerificationPurpose(email, verificationPurpose);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getVerificationCode()).isEqualTo("123456");
        assertThat(found.get().getResendCount()).isZero();
        assertThat(found.get().isVerified()).isFalse();
        assertThat(found.get().getExpiredAt()).isEqualTo(requestedAt.plusMinutes(5));
        assertThat(found.get().getLastSentAt()).isEqualTo(requestedAt);
        assertThat(found.get().getCreatedAt()).isEqualTo(requestedAt);
    }

    @Test
    void 이메일_인증_정보를_삭제한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                "123456",
                LocalDateTime.of(2025, 5, 21, 10, 0)
        );

        emailAuthRedisAdapter.save(emailVerification, Duration.ofMinutes(5));

        // when
        emailAuthRedisAdapter.delete(emailVerification);

        // then
        Optional<EmailVerification> found = emailAuthRedisAdapter.findByEmailAndVerificationPurpose(email, verificationPurpose);

        assertThat(found).isEmpty();
    }

    @Test
    void 저장된_이메일_인증_정보는_TTL이_지나면_만료된다() throws Exception {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        EmailVerification emailVerification = EmailVerification.create(
                email,
                verificationPurpose,
                "123456",
                LocalDateTime.of(2025, 5, 21, 10, 0)
        );

        // when
        emailAuthRedisAdapter.save(emailVerification, Duration.ofMillis(300));

        Thread.sleep(500);

        Optional<EmailVerification> found = emailAuthRedisAdapter.findByEmailAndVerificationPurpose(email, verificationPurpose);

        // then
        assertThat(found).isEmpty();
    }
}