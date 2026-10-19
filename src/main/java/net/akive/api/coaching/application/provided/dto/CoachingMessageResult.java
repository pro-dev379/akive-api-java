package net.akive.api.coaching.application.provided.dto;

import net.akive.api.coaching.domain.CoachingMessage;
import net.akive.api.coaching.domain.MessageCompletionStatus;
import net.akive.api.coaching.domain.MessageRole;

import java.time.Instant;

public record CoachingMessageResult(
        Long coachingMessageId,
        MessageRole role,
        String content,
        MessageCompletionStatus completionStatus,
        Instant createdAt
) {

    public static CoachingMessageResult from(CoachingMessage coachingMessage) {
        return new CoachingMessageResult(
                coachingMessage.getCoachingMessageId(),
                coachingMessage.getRole(),
                coachingMessage.getContent(),
                coachingMessage.getCompletionStatus(),
                coachingMessage.getCreatedAt()
        );
    }
}
