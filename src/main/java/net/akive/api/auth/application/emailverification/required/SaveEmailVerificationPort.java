package net.akive.api.auth.application.emailverification.required;

import net.akive.api.auth.domain.emailverification.EmailVerification;

import java.time.Duration;

public interface SaveEmailVerificationPort {

    void save(EmailVerification emailVerification, Duration ttl);
}
