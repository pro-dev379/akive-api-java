package net.akive.api.coaching.application.provided.dto;

import net.akive.api.coaching.domain.ConversationContext;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record CoachingConversationCreateCommand(Long userId, ConversationContext context) {

    public CoachingConversationCreateCommand {
        requirePositive(userId);
        requireNonNull(context);
    }
}
