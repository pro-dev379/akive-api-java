package net.akive.api.email.application.dto;

import java.util.List;
import java.util.Map;

public record EmailSendCommand(
        String from,
        List<String> to,
        String subject,
        String templatePath,
        Map<String, Object> variables
) {
}
