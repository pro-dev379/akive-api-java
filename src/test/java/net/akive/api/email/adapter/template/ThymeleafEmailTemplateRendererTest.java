package net.akive.api.email.adapter.template;

import net.akive.api.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ThymeleafEmailTemplateRendererTest extends IntegrationTestSupport {

    @Autowired
    ThymeleafEmailTemplateRenderer thymeleafEmailTemplateRenderer;

    @Test
    void 이메일_템플릿을_렌더링한다() {
        String templatePath = "email/verification-code";
        Map<String, Object> variables = Map.of(
                "verificationCode", "123456",
                "expirationMinutes", 5
        );

        String result = thymeleafEmailTemplateRenderer.render(templatePath, variables);

        assertThat(result)
                .contains("이메일을 인증해 주세요")
                .contains("123456")
                .contains("5분")
                .doesNotContain("th:text");
    }
}