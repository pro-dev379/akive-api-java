package net.akive.api.coaching.application.provided.dto;

import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record CoachingTurnResult(String userMessage, String assistantMessage) {

    public CoachingTurnResult {
        userMessage = requireNotBlank(userMessage);
        assistantMessage = requireNotBlank(assistantMessage);
    }
}
