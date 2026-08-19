package net.akive.api.email.domain;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.domain.DomainValidator.requireNotEmpty;

public record EmailMessage(String from, List<String> to, String subject, String content) {

    public EmailMessage {
        from = requireNotBlank(from);
        to = List.copyOf(requireNotEmpty(to));
        requireNotBlank(subject);
        requireNotBlank(content);
    }
}
