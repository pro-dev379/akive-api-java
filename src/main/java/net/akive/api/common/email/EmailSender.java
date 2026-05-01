package net.akive.api.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
public class EmailSender {

    private final SesClient sesClient;
    private static final String DEFAULT_CHARSET = "UTF-8";

    public void sendHtml(EmailMessage emailMessage) {
        SendEmailRequest request = createSendEmailRequest(emailMessage);
        sesClient.sendEmail(request);
    }

    private SendEmailRequest createSendEmailRequest(EmailMessage emailMessage) {
        Destination destination = Destination.builder()
                .toAddresses(emailMessage.to())
                .build();

        Message message = Message.builder()
                .subject(createContent(emailMessage.subject()))
                .body(Body.builder()
                        .html(createContent(emailMessage.content()))
                        .build())
                .build();

        return SendEmailRequest.builder()
                .source(emailMessage.from())
                .destination(destination)
                .message(message)
                .build();
    }

    private Content createContent(String value) {
        return Content.builder()
                .charset(DEFAULT_CHARSET)
                .data(value)
                .build();
    }
}
