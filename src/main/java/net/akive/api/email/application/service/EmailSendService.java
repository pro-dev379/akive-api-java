package net.akive.api.email.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.email.application.provided.dto.EmailSendCommand;
import net.akive.api.email.application.provided.EmailSender;
import net.akive.api.email.application.required.EmailDispatcher;
import net.akive.api.email.application.required.EmailTemplateRenderer;
import net.akive.api.email.domain.EmailMessage;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class EmailSendService implements EmailSender {

    private final EmailDispatcher emailDispatcher;
    private final EmailTemplateRenderer emailTemplateRenderer;

    @Override
    public void send(EmailSendCommand command) {
        String content = emailTemplateRenderer.render(command.templatePath(), command.variables());

        emailDispatcher.dispatch(new EmailMessage(command.from(), command.to(), command.subject(), content));
    }
}
