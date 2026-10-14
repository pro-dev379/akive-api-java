package net.akive.api.learner.application.event;

import net.akive.api.eventprocessing.application.event.IntegrationEvent;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.learner.domain.InsightConfidence;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.InsightType;

import java.time.Instant;
import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;
import static net.akive.api.common.domain.DomainValidator.requirePositive;
import static net.akive.api.eventprocessing.domain.EventType.INSIGHT_CHANGED;

public record LearnerInsightChangedEvent(
        UUID eventId,
        Long userId,
        Long learnerInsightId,
        InsightType insightType,
        String content,
        InsightConfidence confidence,
        InsightStatus status,
        Instant changedAt
) implements IntegrationEvent {

    public LearnerInsightChangedEvent {
        requireNonNull(eventId);
        requirePositive(userId);
        requirePositive(learnerInsightId);
        requireNonNull(insightType);
        content = requireNotBlank(content);
        requireNonNull(confidence);
        requireNonNull(status);
        requireNonNull(changedAt);
    }

    @Override
    public EventType eventType() {
        return INSIGHT_CHANGED;
    }

    @Override
    public Instant createdAt() {
        return this.changedAt;
    }
}
