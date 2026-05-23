package net.akive.api.auth.email.application.port.out;

import net.akive.api.auth.email.domain.EmailAuth;
import net.akive.api.auth.email.domain.EmailAuthPurpose;

import java.time.Duration;
import java.util.Optional;

public interface EmailAuthRepository {

    Optional<EmailAuth> findByEmailAndPurpose(String email, EmailAuthPurpose authPurpose);
    void save(EmailAuth emailAuth, Duration expireDuration);
    void delete(EmailAuth emailAuth);
}
