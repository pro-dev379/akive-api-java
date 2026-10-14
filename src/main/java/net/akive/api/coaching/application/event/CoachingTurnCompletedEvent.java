package net.akive.api.coaching.application.event;

import net.akive.api.eventprocessing.application.event.IntegrationEvent;
import net.akive.api.eventprocessing.domain.EventType;

import java.time.Instant;
import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;

public record CoachingTurnCompletedEvent(
        UUID eventId,
        Long userId,
        Long coachingConversationId,
        Long userMessageId,
        Long assistantMessageId,
        Instant completedAt
) implements IntegrationEvent {

    public CoachingTurnCompletedEvent {
        requireNonNull(eventId);
        requirePositive(userId);
        requirePositive(coachingConversationId);
        requirePositive(userMessageId);
        requirePositive(assistantMessageId);
        requireNonNull(completedAt);
    }

    @Override
    public EventType eventType() {
        return COACHING_TURN_DONE;
    }

    @Override
    public Instant createdAt() {
        return this.completedAt;
    }
}
