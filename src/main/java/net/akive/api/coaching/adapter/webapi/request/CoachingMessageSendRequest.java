package net.akive.api.coaching.adapter.webapi.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import net.akive.api.coaching.application.provided.dto.CoachingMessageSendCommand;

import static net.akive.api.coaching.domain.CoachingPolicy.USER_MESSAGE_CONTENT_MAX_LENGTH;

public record CoachingMessageSendRequest(
        @NotBlank
        @Size(max = USER_MESSAGE_CONTENT_MAX_LENGTH)
        String content
) {

    public CoachingMessageSendCommand to(Long userId, Long coachingConversationId) {
        return new CoachingMessageSendCommand(userId, coachingConversationId, content);
    }
}
