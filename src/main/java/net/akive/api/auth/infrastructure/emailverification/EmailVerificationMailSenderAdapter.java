package net.akive.api.auth.infrastructure.emailverification;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.emailverification.SendEmailVerificationMailPort;
import net.akive.api.common.email.SendEmailService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class EmailVerificationMailSenderAdapter implements SendEmailVerificationMailPort {

    private final SendEmailService sendEmailService;

    @Override
    public void send(String email, String verificationCode, Duration expireDuration) {
        sendEmailService.send(
                List.of(email),
                "[AKIVE] 인증 코드가 도착했어요.",
                "email/verification-code",
                Map.of(
                        "verificationCode", verificationCode,
                        "expireMin", expireDuration.toMinutes()
                )
        );
    }
}
