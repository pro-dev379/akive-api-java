package net.akive.api.email.application.provided.dto;

import java.util.List;
import java.util.Map;

import static net.akive.api.common.domain.DomainValidator.*;

public record EmailSendCommand(
        String from,
        List<String> to,
        String subject,
        String templatePath,
        Map<String, Object> variables
) {

    public EmailSendCommand {
        from = requireNotBlank(from);
        requireNotEmpty(to);
        requireNotBlank(subject);
        templatePath = requireNotBlank(templatePath);
        requireNonNull(variables);
    }
}
