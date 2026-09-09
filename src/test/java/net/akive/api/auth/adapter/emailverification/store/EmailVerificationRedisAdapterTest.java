package net.akive.api.auth.adapter.emailverification.store;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.support.RedisTestSupport;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.createEmailVerification;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.PASSWORD_RESET;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class EmailVerificationRedisAdapterTest extends RedisTestSupport {

    @Test
    void 이메일_인증_정보를_저장하고_조회한다() {
        EmailVerification emailVerification = createEmailVerification();

        emailVerificationStore.save(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);

        Optional<EmailVerification> foundEmailVerification =
                emailVerificationStore.findByEmailAndVerificationPurpose(
                        emailVerification.getEmail(),
                        emailVerification.getVerificationPurpose()
                );

        assertThat(foundEmailVerification)
                .get()
                .satisfies(found -> {
                    assertThat(found.getEmail()).isEqualTo(emailVerification.getEmail());
                    assertThat(found.getVerificationPurpose()).isEqualTo(emailVerification.getVerificationPurpose());
                    assertThat(found.getVerificationCode()).isEqualTo(emailVerification.getVerificationCode());
                    assertThat(found.getRenewalCount()).isZero();
                    assertThat(found.isVerified()).isFalse();
                    assertThat(found.getExpiresAt()).isEqualTo(emailVerification.getExpiresAt());
                    assertThat(found.getLastSentAt()).isEqualTo(emailVerification.getLastSentAt());
                    assertThat(found.getCreatedAt()).isEqualTo(emailVerification.getCreatedAt());
                });
    }

    @Test
    void 같은_이메일의_인증_정보는_목적별로_분리되어_저장된다() {
        EmailVerification signUpEmailVerification = createEmailVerification(SIGN_UP);
        EmailVerification findPasswordEmailVerification = createEmailVerification(PASSWORD_RESET);

        emailVerificationStore.save(signUpEmailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);
        emailVerificationStore.save(findPasswordEmailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);

        Optional<EmailVerification> foundSignUpEmailVerification =
                emailVerificationStore.findByEmailAndVerificationPurpose(
                        signUpEmailVerification.getEmail(),
                        SIGN_UP
                );
        Optional<EmailVerification> foundFindPasswordEmailVerification =
                emailVerificationStore.findByEmailAndVerificationPurpose(
                        findPasswordEmailVerification.getEmail(),
                        PASSWORD_RESET
                );

        assertThat(foundSignUpEmailVerification)
                .get()
                .extracting(EmailVerification::getVerificationPurpose)
                .isEqualTo(SIGN_UP);
        assertThat(foundFindPasswordEmailVerification)
                .get()
                .extracting(EmailVerification::getVerificationPurpose)
                .isEqualTo(PASSWORD_RESET);
    }

    @Test
    void 이메일_인증_정보를_삭제한다() {
        EmailVerification emailVerification = createEmailVerification();

        emailVerificationStore.save(emailVerification, VERIFICATION_CODE_EXPIRATION_DURATION);

        emailVerificationStore.delete(emailVerification);

        Optional<EmailVerification> foundEmailVerification =
                emailVerificationStore.findByEmailAndVerificationPurpose(
                        emailVerification.getEmail(),
                        emailVerification.getVerificationPurpose()
                );

        assertThat(foundEmailVerification).isEmpty();
    }

    @Test
    void 저장된_이메일_인증_정보는_TTL이_지나면_만료된다() {
        EmailVerification emailVerification = createEmailVerification();

        emailVerificationStore.save(emailVerification, Duration.ofMillis(300));

        assertThat(
                emailVerificationStore.findByEmailAndVerificationPurpose(
                        emailVerification.getEmail(),
                        emailVerification.getVerificationPurpose()
                )
        ).isPresent();

        await().atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    Optional<EmailVerification> found =
                            emailVerificationStore.findByEmailAndVerificationPurpose(
                                    emailVerification.getEmail(),
                                    emailVerification.getVerificationPurpose()
                            );

                    assertThat(found).isEmpty();
                });
    }
}
