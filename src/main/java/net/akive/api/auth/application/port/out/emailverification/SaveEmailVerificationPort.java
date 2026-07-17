package net.akive.api.auth.application.port.out.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerification;

import java.time.Duration;

public interface SaveEmailVerificationPort {

    void save(EmailVerification emailVerification, Duration ttl);
}
