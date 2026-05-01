package net.akive.api.common.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailTemplateRenderer {

    private final SpringTemplateEngine templateEngine;

    public String render(String templatePath, Map<String, Object> variables) {
        Context context = new Context();
        variables.forEach(context::setVariable);

        return templateEngine.process(templatePath, context);
    }
}
