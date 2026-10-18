package net.akive.api.coaching.application.provided.dto;

import net.akive.api.coaching.application.required.data.CoachingConversationSummaryData;
import net.akive.api.coaching.domain.ConversationContext;

import java.time.Instant;

public record CoachingConversationSummaryResult(
        Long coachingConversationId,
        String title,
        ConversationContext context,
        String preview,
        Instant lastMessageCreatedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static CoachingConversationSummaryResult from(CoachingConversationSummaryData summary) {
        return new CoachingConversationSummaryResult(
                summary.coachingConversationId(),
                summary.title(),
                summary.context(),
                summary.preview(),
                summary.lastMessageCreatedAt(),
                summary.createdAt(),
                summary.updatedAt()
        );
    }
}
