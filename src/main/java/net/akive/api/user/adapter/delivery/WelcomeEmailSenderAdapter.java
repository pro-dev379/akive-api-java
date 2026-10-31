package net.akive.api.user.adapter.delivery;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.domain.Email;
import net.akive.api.core.config.email.EmailProperties;
import net.akive.api.email.application.provided.EmailSender;
import net.akive.api.email.application.provided.dto.EmailSendCommand;
import net.akive.api.user.application.required.WelcomeEmailSender;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WelcomeEmailSenderAdapter implements WelcomeEmailSender {

    private final EmailSender emailSender;
    private final EmailProperties emailProperties;

    @Override
    public void send(Email email, String nickname) {
        EmailSendCommand command = new EmailSendCommand(
                emailProperties.from().welcome(),
                List.of(email.value()),
                "akive와 함께 학습의 모든 순간을 이어가세요",
                "email/welcome",
                Map.of(
                        "nickname", nickname,
                        "supportEmail", emailProperties.from().support()
                )
        );

        emailSender.send(command);
    }
}
