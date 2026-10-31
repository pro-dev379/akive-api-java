package net.akive.api.auth.adapter.emailverification.delivery;

import lombok.RequiredArgsConstructor;
import net.akive.api.auth.application.emailverification.required.EmailVerificationSender;
import net.akive.api.auth.domain.emailverification.EmailVerification;
import net.akive.api.core.config.email.EmailProperties;
import net.akive.api.email.application.provided.EmailSender;
import net.akive.api.email.application.provided.dto.EmailSendCommand;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class EmailVerificationSenderAdapter implements EmailVerificationSender {

    private final EmailSender emailSender;
    private final EmailProperties emailProperties;

    @Override
    public void send(EmailVerification emailVerification, Duration expirationDuration) {
        EmailSendCommand command = new EmailSendCommand(
                emailProperties.from().verification(),
                List.of(emailVerification.getEmail().value()),
                "[akive] 이메일 인증 코드가 도착했어요",
                "email/verification-code",
                Map.of(
                        "verificationCode", emailVerification.getVerificationCode().value(),
                        "expirationMinutes", expirationDuration.toMinutes()
                )
        );

        emailSender.send(command);
    }
}
