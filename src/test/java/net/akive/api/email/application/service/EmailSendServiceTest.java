package net.akive.api.email.application.service;

import net.akive.api.email.application.provided.dto.EmailSendCommand;
import net.akive.api.email.application.required.EmailDispatcher;
import net.akive.api.email.application.required.EmailTemplateRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailSendServiceTest {

    @Mock
    EmailDispatcher emailDispatcher;

    @Mock
    EmailTemplateRenderer emailTemplateRenderer;

    @InjectMocks
    EmailSendService emailSendService;

    @Test
    void 이메일을_발송한다() {
        EmailSendCommand command = new EmailSendCommand(
                "from@test.com",
                List.of("to@test.com"),
                "subject",
                "email/hello.html",
                Map.of("text", "hello")
        );
        String content = "<h1>hello</h1>";
        given(emailTemplateRenderer.render(command.templatePath(), command.variables())).willReturn(content);

        emailSendService.send(command);

        then(emailTemplateRenderer).should().render(command.templatePath(), command.variables());
        then(emailDispatcher).should().dispatch(argThat(emailMessage ->
                emailMessage.from().equals(command.from())
                        && emailMessage.to().equals(command.to())
                        && emailMessage.subject().equals(command.subject())
                        && emailMessage.content().equals(content)
        ));
    }
}
