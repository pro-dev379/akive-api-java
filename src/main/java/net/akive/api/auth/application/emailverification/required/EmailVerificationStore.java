package net.akive.api.auth.application.emailverification.required;

import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.domain.Email;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationStore {

    Optional<EmailVerification> findByEmailAndVerificationPurpose(
            Email email,
            EmailVerificationPurpose verificationPurpose
    );

    void save(EmailVerification emailVerification, Duration ttl);

    void delete(EmailVerification emailVerification);
}
