package net.akive.api.auth.email.application.port.out;

import net.akive.api.auth.email.domain.EmailAuth;
import net.akive.api.auth.email.domain.EmailAuthPurpose;

import java.time.Duration;
import java.util.Optional;

public interface EmailAuthRepository {

    Optional<EmailAuth> findByPurposeAndEmail(EmailAuthPurpose authPurpose, String email);
    void save(EmailAuthPurpose authPurpose, String email, EmailAuth emailAuth, Duration expireDuration);
    void delete(EmailAuthPurpose authPurpose, String email);
}
