package net.akive.api.learner.application.prompt;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record LearnerInsightPrompt(String instructions, String input) {

    public LearnerInsightPrompt {
        instructions = requireNotBlank(instructions);
        input = requireNotBlank(input);
    }
}
