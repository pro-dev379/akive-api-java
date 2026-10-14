package net.akive.api.coaching.application.prompt;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record CoachingPromptComposition(String message, List<ContextSection> contextSections) {

    public CoachingPromptComposition {
        message = requireNotBlank(message);
        contextSections = List.copyOf(requireNonNull(contextSections));
    }

    public static CoachingPromptComposition from(String message) {
        return new CoachingPromptComposition(message, List.of());
    }

    public record ContextSection(String name, String content) {

        public ContextSection {
            name = requireNotBlank(name);
            content = requireNotBlank(content);
        }
    }
}
