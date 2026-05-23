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

    @Override
    public Optional<EmailAuth> findByEmailAndPurpose(String email, EmailAuthPurpose authPurpose) {
        return redisTemplateClient.get(createKey(authPurpose, email), EmailAuth.class);
    }

    @Override
    public void save(EmailAuth emailAuth, Duration expireDuration) {
        EmailAuthPurpose authPurpose = emailAuth.getAuthPurpose();
        String email = emailAuth.getEmail();
        redisTemplateClient.set(createKey(authPurpose, email), emailAuth, expireDuration);
    }

    @Override
    public void delete(EmailAuth emailAuth) {
        EmailAuthPurpose authPurpose = emailAuth.getAuthPurpose();
        String email = emailAuth.getEmail();
        redisTemplateClient.delete(createKey(authPurpose, email));
    }

    private String createKey(EmailAuthPurpose authPurpose, String email) {
        return CacheKeyProvider.emailAuth(email, authPurpose);
    }
}
