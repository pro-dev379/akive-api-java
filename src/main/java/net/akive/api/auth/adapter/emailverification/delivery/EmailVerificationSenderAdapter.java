package net.akive.api.auth.adapter.emailverification.delivery;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.required.EmailVerificationSender;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.email.application.provided.dto.EmailSendCommand;
import net.akive.api.email.application.provided.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class EmailVerificationSenderAdapter implements EmailVerificationSender {

    @Value("${email.from.verification}")
    private String from;

    private final EmailSender emailSender;

    @Override
    public void send(EmailVerification emailVerification, Duration expirationDuration) {
        EmailSendCommand command = new EmailSendCommand(
                from,
                List.of(emailVerification.getEmail().value()),
                "[AKIVE] 인증 코드가 도착했어요.",
                "email/verification-code",
                Map.of(
                        "verificationCode", emailVerification.getVerificationCode().value(),
                        "expirationMinutes", expirationDuration.toMinutes()
                )
        );

        emailSender.send(command);
    }
}
