package net.akive.api.coaching.adapter.webapi.response;

import net.akive.api.coaching.application.provided.dto.CoachingConversationSummaryResult;
import net.akive.api.coaching.domain.ConversationContext;
import net.akive.api.common.pagination.CursorInfo;
import net.akive.api.common.pagination.CursorResult;

import java.time.Instant;
import java.util.List;

public record CoachingConversationsFindResponse(
        CursorInfo<Long> cursorInfo,
        List<CoachingConversationResponse> coachingConversations
) {

    public static CoachingConversationsFindResponse from(
            CursorResult<CoachingConversationSummaryResult, Long> summaryPage
    ) {
        return new CoachingConversationsFindResponse(
                summaryPage.cursorInfo(),
                summaryPage.contents().stream()
                        .map(CoachingConversationResponse::from)
                        .toList()
        );
    }

    public record CoachingConversationResponse(
            Long coachingConversationId,
            String title,
            ConversationContext context,
            String preview,
            Instant lastMessageCreatedAt,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static CoachingConversationResponse from(CoachingConversationSummaryResult summary) {
            return new CoachingConversationResponse(
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
}
