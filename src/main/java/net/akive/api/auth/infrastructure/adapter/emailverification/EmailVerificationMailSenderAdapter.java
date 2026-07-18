package net.akive.api.auth.infrastructure.adapter.emailverification;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.port.out.emailverification.SendEmailVerificationMailPort;
import net.akive.api.auth.domain.emailverification.EmailVerification;
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
    public void send(EmailVerification emailVerification, Duration expireDuration) {
        sendEmailService.send(
                List.of(emailVerification.getEmail().value()),
                "[AKIVE] 인증 코드가 도착했어요.",
                "email/verification-code",
                Map.of(
                        "verificationCode", emailVerification.getVerificationCode().value(),
                        "expireMin", expireDuration.toMinutes()
                )
        );
    }
}
