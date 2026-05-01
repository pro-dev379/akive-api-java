package net.akive.api.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailService {

    private final EmailSender emailSender;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final static String FROM = "akive@akive.net";

    public void sendHtml(EmailMessage emailMessage) {
        emailSender.sendHtml(emailMessage);
    }

    public void sendTemplateHtml(
            List<String> to,
            String subject,
            String templatePath,
            Map<String, Object> variables
    ) {
        String content = emailTemplateRenderer.render(templatePath, variables);

        EmailMessage emailMessage = EmailMessage.builder()
                .from(FROM)
                .to(to)
                .subject(subject)
                .content(content)
                .build();

        emailSender.sendHtml(emailMessage);
    }
}
