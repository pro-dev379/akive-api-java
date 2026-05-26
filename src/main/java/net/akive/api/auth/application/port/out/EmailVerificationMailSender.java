package net.akive.api.auth.application.port.out;

import java.time.Duration;

public interface EmailVerificationMailSender {

    void send(String email, String verificationCode, Duration expireDuration);
}
