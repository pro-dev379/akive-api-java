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
        sesClient.sendEmail(toSendEmailRequest(emailMessage));
    }

    private SendEmailRequest toSendEmailRequest(EmailMessage emailMessage) {
        return SendEmailRequest.builder()
                .source(emailMessage.from())
                .destination(toDestination(emailMessage))
                .message(toSesMessage(emailMessage))
                .build();
    }

    private Destination toDestination(EmailMessage emailMessage) {
        return Destination.builder()
                .toAddresses(emailMessage.to())
                .build();
    }

    private Message toSesMessage(EmailMessage emailMessage) {
        return Message.builder()
                .subject(toContent(emailMessage.subject()))
                .body(Body.builder()
                        .html(toContent(emailMessage.content()))
                        .build())
                .build();
    }

    private Content toContent(String value) {
        return Content.builder()
                .charset(DEFAULT_CHARSET)
                .data(value)
                .build();
    }
}
