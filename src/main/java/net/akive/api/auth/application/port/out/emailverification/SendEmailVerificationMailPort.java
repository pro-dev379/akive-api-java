package net.akive.api.auth.application.port.out.emailverification;

import java.time.Duration;

public interface SendEmailVerificationMailPort {

    void send(String email, String verificationCode, Duration expireDuration);
}
