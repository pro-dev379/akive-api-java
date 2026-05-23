package net.akive.api.auth.email.infrastructure;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.email.application.port.out.EmailAuthMailSender;
import net.akive.api.common.email.EmailSenderService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailAuthMailSenderAdapter implements EmailAuthMailSender {

    private final EmailSenderService emailSenderService;

    @Override
    public void sendAuthCode(String email, String authCode, Duration expireDuration) {
        emailSenderService.sendTemplateHtml(
                List.of(email),
                "[AKIVE] 인증 코드가 도착했어요.",
                "email/auth-code",
                Map.of(
                        "authCode", authCode,
                        "expireMin", expireDuration.toMinutes()
                )
        );
    }
}
