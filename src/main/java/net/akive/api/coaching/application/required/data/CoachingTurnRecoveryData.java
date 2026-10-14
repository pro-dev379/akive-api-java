package net.akive.api.coaching.application.required.data;

import net.akive.api.coaching.domain.MessageCompletionStatus;

import java.util.UUID;

public record CoachingTurnRecoveryData(
        UUID eventId,
        Long userMessageId,
        String content,
        MessageCompletionStatus completionStatus
) {
}
