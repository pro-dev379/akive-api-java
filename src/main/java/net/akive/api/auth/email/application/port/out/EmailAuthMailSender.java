package net.akive.api.auth.email.application.port.out;

import java.time.Duration;

public interface EmailAuthMailSender {

    void sendAuthCode(String email, String authCode, Duration expireDuration);
}
