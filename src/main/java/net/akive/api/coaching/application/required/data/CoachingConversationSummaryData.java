package net.akive.api.coaching.application.required.data;

import net.akive.api.coaching.domain.ConversationContext;

import java.time.Instant;

public record CoachingConversationSummaryData(
        Long coachingConversationId,
        String title,
        ConversationContext context,
        String preview,
        Instant lastMessageCreatedAt,
        Instant createdAt,
        Instant updatedAt,
        Long latestMessageId
) {
}
