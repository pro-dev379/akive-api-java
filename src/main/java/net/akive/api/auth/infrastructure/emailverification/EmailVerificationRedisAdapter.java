package net.akive.api.auth.infrastructure.emailverification;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.emailverification.EmailVerificationRepository;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.cache.CacheKeyProvider;
import net.akive.api.common.cache.RedisTemplateClient;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class EmailVerificationRedisAdapter implements EmailVerificationRepository {

    private final RedisTemplateClient redisTemplateClient;

    @Override
    public Optional<EmailVerification> findByEmailAndVerificationPurpose(String email, EmailVerificationPurpose verificationPurpose) {
        return redisTemplateClient.get(createKey(verificationPurpose, email), EmailVerification.class);
    }

    @Override
    public void save(EmailVerification emailVerification, Duration ttl) {
        EmailVerificationPurpose verificationPurpose = emailVerification.getVerificationPurpose();
        String email = emailVerification.getEmail();
        redisTemplateClient.set(createKey(verificationPurpose, email), emailVerification, ttl);
    }

    @Override
    public void delete(EmailVerification emailVerification) {
        EmailVerificationPurpose verificationPurpose = emailVerification.getVerificationPurpose();
        String email = emailVerification.getEmail();
        redisTemplateClient.delete(createKey(verificationPurpose, email));
    }

    private String createKey(EmailVerificationPurpose verificationPurpose, String email) {
        return CacheKeyProvider.emailVerification(email, verificationPurpose);
    }
}
