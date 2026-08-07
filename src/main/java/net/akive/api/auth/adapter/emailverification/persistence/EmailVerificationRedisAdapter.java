package net.akive.api.auth.adapter.emailverification.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.required.DeleteEmailVerificationPort;
import net.akive.api.auth.application.emailverification.required.LoadEmailVerificationPort;
import net.akive.api.auth.application.emailverification.required.SaveEmailVerificationPort;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.cache.CacheKeyProvider;
import net.akive.api.common.cache.RedisTemplateClient;
import net.akive.api.common.domain.Email;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class EmailVerificationRedisAdapter implements
        LoadEmailVerificationPort,
        SaveEmailVerificationPort,
        DeleteEmailVerificationPort {

    private final RedisTemplateClient redisTemplateClient;

    @Override
    public Optional<EmailVerification> loadByEmailAndVerificationPurpose(
            Email email,
            EmailVerificationPurpose verificationPurpose
    ) {
        return redisTemplateClient.get(
                createKey(verificationPurpose, email.value()),
                EmailVerification.class
        );
    }

    @Override
    public void save(EmailVerification emailVerification, Duration ttl) {
        EmailVerificationPurpose verificationPurpose = emailVerification.getVerificationPurpose();
        String email = emailVerification.getEmail().value();
        redisTemplateClient.set(
                createKey(verificationPurpose, email),
                emailVerification,
                ttl
        );
    }

    @Override
    public void delete(EmailVerification emailVerification) {
        EmailVerificationPurpose verificationPurpose = emailVerification.getVerificationPurpose();
        String email = emailVerification.getEmail().value();
        redisTemplateClient.delete(createKey(verificationPurpose, email));
    }

    private String createKey(EmailVerificationPurpose verificationPurpose, String email) {
        return CacheKeyProvider.emailVerification(email, verificationPurpose);
    }
}
