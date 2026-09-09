package net.akive.api.email.application.service;

import net.akive.api.email.application.provided.dto.EmailSendCommand;
import net.akive.api.email.application.required.EmailDispatcher;
import net.akive.api.email.application.required.EmailTemplateRenderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.email.application.fixture.EmailSendCommandFixture.RENDERED_CONTENT;
import static net.akive.api.email.application.fixture.EmailSendCommandFixture.createEmailSendCommand;
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
        EmailSendCommand command = createEmailSendCommand();
        given(emailTemplateRenderer.render(command.templatePath(), command.variables()))
                .willReturn(RENDERED_CONTENT);

        emailSendService.send(command);

        then(emailTemplateRenderer).should().render(command.templatePath(), command.variables());
        then(emailDispatcher).should().dispatch(argThat(emailMessage ->
                emailMessage.from().equals(command.from())
                        && emailMessage.to().equals(command.to())
                        && emailMessage.subject().equals(command.subject())
                        && emailMessage.content().equals(RENDERED_CONTENT)
        ));
    }
}
