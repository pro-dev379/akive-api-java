package net.akive.api.auth.email.application.port.out;

import net.akive.api.auth.email.domain.EmailVerification;
import net.akive.api.auth.email.domain.EmailVerificationPurpose;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationRepository {

    Optional<EmailVerification> findByEmailAndVerificationPurpose(String email, EmailVerificationPurpose verificationPurpose);
    void save(EmailVerification emailVerification, Duration expireDuration);
    void delete(EmailVerification emailVerification);
}
