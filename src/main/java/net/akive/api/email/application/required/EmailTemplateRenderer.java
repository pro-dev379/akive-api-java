package net.akive.api.email.application.required;

import java.util.Map;

public interface EmailTemplateRenderer {

    String render(String templatePath, Map<String, Object> variables);
}
