package net.akive.api.coaching.application.provided.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record CoachingTurnFindQuery(
        Long userId,
        Long coachingConversationId,
        Long userMessageId,
        Long assistantMessageId
) {

    public CoachingTurnFindQuery {
        requirePositive(userId);
        requirePositive(coachingConversationId);
        requirePositive(userMessageId);
        requirePositive(assistantMessageId);
    }
}
