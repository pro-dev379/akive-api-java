package net.akive.api.coaching.adapter.webapi.response;

import net.akive.api.coaching.application.provided.dto.CoachingMessageResult;
import net.akive.api.coaching.domain.MessageCompletionStatus;
import net.akive.api.coaching.domain.MessageRole;
import net.akive.api.common.pagination.CursorInfo;
import net.akive.api.common.pagination.CursorResult;

import java.time.Instant;
import java.util.List;

public record CoachingMessagesFindResponse(
        CursorInfo<Long> cursorInfo,
        List<CoachingMessageResponse> coachingMessages
) {

    public static CoachingMessagesFindResponse from(CursorResult<CoachingMessageResult, Long> messagePage) {
        return new CoachingMessagesFindResponse(
                messagePage.cursorInfo(),
                messagePage.contents().stream()
                        .map(CoachingMessageResponse::from)
                        .toList()
        );
    }

    public record CoachingMessageResponse(
            Long coachingMessageId,
            MessageRole role,
            String content,
            MessageCompletionStatus completionStatus,
            Instant createdAt
    ) {

        public static CoachingMessageResponse from(CoachingMessageResult message) {
            return new CoachingMessageResponse(
                    message.coachingMessageId(),
                    message.role(),
                    message.content(),
                    message.completionStatus(),
                    message.createdAt()
            );
        }
    }
}
