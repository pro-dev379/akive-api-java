package net.akive.api.email.adapter.template;

import lombok.RequiredArgsConstructor;
import net.akive.api.email.application.required.EmailTemplateRenderer;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Component
@RequiredArgsConstructor
class ThymeleafEmailTemplateRenderer implements EmailTemplateRenderer {

    private final SpringTemplateEngine templateEngine;

    @Override
    public String render(String templatePath, Map<String, Object> variables) {
        Context context = new Context();
        variables.forEach(context::setVariable);

        return templateEngine.process(templatePath, context);
    }
}
