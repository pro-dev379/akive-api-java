package net.akive.api.common.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

    @Mock
    SesClient sesClient;

    @InjectMocks
    EmailSender emailSender;

    @Test
    void HTML_이메일을_발송한다() {
        // given
        String destination = "test@test.com";
        String subject = "이메일 인증";
        String content = "<h1>인증 코드: 123456</h1>";
        EmailMessage emailMessage = EmailMessage.of(
                List.of(destination),
                subject,
                content
        );

        // when
        emailSender.sendHtml(emailMessage);

        // then
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        then(sesClient).should().sendEmail(captor.capture());

        SendEmailRequest request = captor.getValue();

        assertThat(request.source()).isEqualTo(emailMessage.from());

        assertThat(request.destination().toAddresses())
                .containsExactly(destination);

        assertThat(request.message().subject().data())
                .isEqualTo(subject);

        assertThat(request.message().subject().charset())
                .isEqualTo("UTF-8");

        assertThat(request.message().body().html().data())
                .isEqualTo(content);

        assertThat(request.message().body().html().charset())
                .isEqualTo("UTF-8");
    }
}
