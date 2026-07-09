package net.akive.api.auth.application.port.out.emailverification;

import net.akive.api.auth.domain.emailverification.EmailVerification;

import java.time.Duration;

public interface SendEmailVerificationMailPort {

    void send(EmailVerification emailVerification, Duration expireDuration);
}
