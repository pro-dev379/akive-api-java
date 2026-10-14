package net.akive.api.coaching.adapter.webapi.request;

import jakarta.validation.constraints.NotNull;
import net.akive.api.coaching.application.provided.dto.CoachingConversationCreateCommand;
import net.akive.api.coaching.domain.ConversationContext;

public record CoachingConversationCreateRequest(@NotNull ConversationContext context) {

    public CoachingConversationCreateCommand to(Long userId) {
        return new CoachingConversationCreateCommand(userId, context);
    }
}
