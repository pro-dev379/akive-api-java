package net.akive.api.auth.application.port.out.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationRepository {

    Optional<EmailVerification> findByEmailAndVerificationPurpose(String email, EmailVerificationPurpose verificationPurpose);
    void save(EmailVerification emailVerification, Duration expireDuration);
    void delete(EmailVerification emailVerification);
}
