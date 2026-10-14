package net.akive.api.coaching.application.prompt;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record CoachingPrompt(String instructions, String input) {

    public CoachingPrompt {
        instructions = requireNotBlank(instructions);
        input = requireNotBlank(input);
    }
}
