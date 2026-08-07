package net.akive.api.auth.application.emailverification.required;

import net.akive.api.auth.domain.emailverification.EmailVerification;

import java.time.Duration;

public interface SendEmailVerificationMailPort {

    void send(EmailVerification emailVerification, Duration expireDuration);
}
