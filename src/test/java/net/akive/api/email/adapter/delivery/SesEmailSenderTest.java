package net.akive.api.email.adapter.delivery;

import net.akive.api.email.domain.EmailMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import static net.akive.api.email.domain.EmailMessageFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SesEmailSenderTest {

    @Mock
    SesClient sesClient;

    @InjectMocks
    SesEmailSender sesEmailSender;

    @Test
    void 이메일을_SES로_dispatch한다() {
        EmailMessage emailMessage = new EmailMessage(FROM, TO, SUBJECT, CONTENT);

        sesEmailSender.dispatch(emailMessage);

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor
                .forClass(SendEmailRequest.class);
        then(sesClient)
                .should()
                .sendEmail(captor.capture());

        SendEmailRequest request = captor.getValue();

        assertThat(request.source()).isEqualTo(emailMessage.from());
        assertThat(request.destination().toAddresses()).isEqualTo(emailMessage.to());
        assertThat(request.message().subject().data()).isEqualTo(emailMessage.subject());
        assertThat(request.message().subject().charset()).isEqualTo("UTF-8");
        assertThat(request.message().body().html().data()).isEqualTo(emailMessage.content());
        assertThat(request.message().body().html().charset()).isEqualTo("UTF-8");
    }
}
