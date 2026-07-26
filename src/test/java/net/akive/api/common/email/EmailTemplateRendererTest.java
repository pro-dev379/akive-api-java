package net.akive.api.common.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmailTemplateRendererTest {

    @Mock
    SpringTemplateEngine templateEngine;

    @InjectMocks
    EmailTemplateRenderer emailTemplateRenderer;

    @Test
    void 이메일_템플릿을_렌더링한다() {
        // given
        String templatePath = "email/verification-code";
        Map<String, Object> variables = Map.of(
                "verificationCode", "123456",
                "expiresInMinutes", 5
        );

        String renderedContent = "<h1>인증 코드 : 123456</h1>";

        given(templateEngine.process(eq(templatePath), any(Context.class)))
                .willReturn(renderedContent);

        // when
        String result = emailTemplateRenderer.render(templatePath, variables);

        // then
        assertThat(result).isEqualTo(renderedContent);

        then(templateEngine).should().process(
                eq(templatePath),
                argThat(context ->
                        context.getVariable("verificationCode").equals("123456")
                                && context.getVariable("expiresInMinutes").equals(5)
                )
        );
    }
}
