package net.akive.api.auth.application.port.out.emailverification;

import java.time.Duration;

public interface EmailVerificationMailSender {

    void send(String email, String verificationCode, Duration expireDuration);
}
