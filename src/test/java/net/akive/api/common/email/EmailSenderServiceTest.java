package net.akive.api.common.email;

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
class EmailSenderServiceTest {

    @Mock
    EmailSender emailSender;

    @Mock
    EmailTemplateRenderer emailTemplateRenderer;

    @InjectMocks
    EmailSenderService emailSenderService;

    @Test
   void 이메일_템플릿을_렌더링하여_HTML_이메일을_발송한다() {
        // given
        List<String> to = List.of("test@test.com");
        String subject = "이메일 인증 코드";
        String templatePath = "email/verification-code";
        Map<String, Object> variables = Map.of(
                "verificationCode", "123456",
                "expireMin", 5
        );

        String content = "<h1>인증 코드 : 123456</h1>";

        given(emailTemplateRenderer.render(templatePath, variables))
                .willReturn(content);

        // when
        emailSenderService.send(to, subject, templatePath, variables);

        // then
        then(emailTemplateRenderer).should().render(templatePath, variables);
        then(emailSender).should().sendHtml(argThat(emailMessage ->
                emailMessage.to().equals(to)
                        && emailMessage.subject().equals(subject)
                        && emailMessage.content().equals(content)
        ));
    }
}