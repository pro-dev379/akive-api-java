package net.akive.api.coaching.application.provided.dto;

import static net.akive.api.coaching.domain.CoachingPolicy.USER_MESSAGE_CONTENT_MAX_LENGTH;
import static net.akive.api.common.domain.DomainValidator.*;

public record CoachingMessageSendCommand(
        Long userId,
        Long coachingConversationId,
        String content
) {

    public CoachingMessageSendCommand {
        requirePositive(userId);
        requirePositive(coachingConversationId);
        content = requireNotBlank(content);
        requireMaxLength(content, USER_MESSAGE_CONTENT_MAX_LENGTH);
    }
}
