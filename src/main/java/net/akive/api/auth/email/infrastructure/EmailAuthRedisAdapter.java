package net.akive.api.auth.email.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.port.out.EmailAuthRepository;
import net.akive.api.auth.email.domain.EmailAuth;
import net.akive.api.auth.email.domain.EmailAuthPurpose;
import net.akive.api.common.cache.CacheKeyProvider;
import net.akive.api.common.cache.RedisTemplateClient;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailAuthRedisAdapter implements EmailAuthRepository {

    private final RedisTemplateClient redisTemplateClient;

    public Optional<EmailAuth> findByPurposeAndEmail(EmailAuthPurpose authPurpose, String email) {
        return redisTemplateClient.get(createKey(authPurpose, email), EmailAuth.class);
    }

    public void save(EmailAuthPurpose authPurpose, String email, EmailAuth emailAuth, Duration expireDuration) {
        redisTemplateClient.set(createKey(authPurpose, email), emailAuth, expireDuration);
    }

    public void delete(EmailAuthPurpose authPurpose, String email) {
        redisTemplateClient.delete(createKey(authPurpose, email));
    }

    private String createKey(EmailAuthPurpose authPurpose, String email) {
        return CacheKeyProvider.emailAuth(authPurpose, email);
    }
}
