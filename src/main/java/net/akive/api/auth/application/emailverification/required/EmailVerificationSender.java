package net.akive.api.auth.application.emailverification.required;

import net.akive.api.auth.domain.emailverification.EmailVerification;

import java.time.Duration;

public interface EmailVerificationSender {

    void send(EmailVerification emailVerification, Duration expirationDuration);
}
