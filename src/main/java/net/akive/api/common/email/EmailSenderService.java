package net.akive.api.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailSenderService {

    private final EmailSender emailSender;
    private final EmailTemplateRenderer emailTemplateRenderer;

    public void sendTemplateHtml(
            List<String> to,
            String subject,
            String templatePath,
            Map<String, Object> variables
    ) {
        String content = emailTemplateRenderer.render(templatePath, variables);
        EmailMessage emailMessage = EmailMessage.of(to, subject, content);
        emailSender.sendHtml(emailMessage);
    }
}
