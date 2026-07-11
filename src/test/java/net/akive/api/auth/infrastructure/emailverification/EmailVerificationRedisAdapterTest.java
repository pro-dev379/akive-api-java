package net.akive.api.auth.infrastructure.emailverification;

import net.akive.api.common.domain.Email;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationCode;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.support.RedisTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.*;
import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

class EmailVerificationRedisAdapterTest extends RedisTestSupport {

    @Test
    void 이메일_인증_정보를_저장하고_조회한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        LocalDateTime requestedAt = LocalDateTime.of(2025, 5, 21, 10, 0);
        EmailVerification emailVerification = EmailVerification.create(
                Email.of(email),
                verificationPurpose,
                EmailVerificationCode.of("123456"),
                requestedAt
        );

        // when
        saveEmailVerificationPort.save(emailVerification, Duration.ofMinutes(5));
        Optional<EmailVerification> found =
                loadEmailVerificationPort.loadByEmailAndVerificationPurpose(Email.of(email), verificationPurpose);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getVerificationCode()).isEqualTo(EmailVerificationCode.of("123456"));
        assertThat(found.get().getResendCount()).isZero();
        assertThat(found.get().isVerified()).isFalse();
        assertThat(found.get().getExpiresAt()).isEqualTo(requestedAt.plusMinutes(5));
        assertThat(found.get().getLastSentAt()).isEqualTo(requestedAt);
        assertThat(found.get().getCreatedAt()).isEqualTo(requestedAt);
    }

    @Test
    void 이메일_인증_정보를_삭제한다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        EmailVerification emailVerification = EmailVerification.create(
                Email.of(email),
                verificationPurpose,
                EmailVerificationCode.of("123456"),
                LocalDateTime.of(2025, 5, 21, 10, 0)
        );

        saveEmailVerificationPort.save(emailVerification, Duration.ofMinutes(5));

        // when
        deleteEmailVerificationPort.delete(emailVerification);

        // then
        Optional<EmailVerification> found =
                loadEmailVerificationPort.loadByEmailAndVerificationPurpose(Email.of(email), verificationPurpose);

        assertThat(found).isEmpty();
    }

    @Test
    void 저장된_이메일_인증_정보는_TTL이_지나면_만료된다() {
        // given
        String email = "test@test.com";
        EmailVerificationPurpose verificationPurpose = SIGN_UP;
        EmailVerification emailVerification = EmailVerification.create(
                Email.of(email),
                verificationPurpose,
                EmailVerificationCode.of("123456"),
                LocalDateTime.of(2025, 5, 21, 10, 0)
        );

        // when
        saveEmailVerificationPort.save(emailVerification, Duration.ofMillis(300));

        // then
        await()
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    Optional<EmailVerification> found =
                            loadEmailVerificationPort.loadByEmailAndVerificationPurpose(
                                    Email.of(email),
                                    verificationPurpose
                            );

                    assertThat(found).isEmpty();
                });
    }
}
