package net.akive.api.eventprocessing.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;
import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_PRODUCED_EVENT_STATE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_PRODUCED_EVENT_STATUS_TRANSITION;
import static net.akive.api.eventprocessing.domain.ProducedEventStatus.*;

@Getter
@Builder(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class ProducedEvent {

    private Long producedEventId;

    private UUID eventId;

    private EventType eventType;

    private String payload;

    private ProducedEventStatus status;

    private int retryCount;

    private Instant nextAttemptAt;

    private Instant processingStartedAt;

    private Instant publishedAt;

    private Instant createdAt;

    public static ProducedEvent reconstruct(
            Long producedEventId,
            UUID eventId,
            EventType eventType,
            String payload,
            ProducedEventStatus status,
            int retryCount,
            Instant nextAttemptAt,
            Instant processingStartedAt,
            Instant publishedAt,
            Instant createdAt
    ) {
        ProducedEvent producedEvent = ProducedEvent.builder()
                .producedEventId(requirePositive(producedEventId))
                .eventId(requireNonNull(eventId))
                .eventType(requireNonNull(eventType))
                .payload(requireNotBlank(payload))
                .status(requireNonNull(status))
                .retryCount(requireNonNegative(retryCount))
                .nextAttemptAt(requireNonNull(nextAttemptAt))
                .processingStartedAt(processingStartedAt)
                .publishedAt(publishedAt)
                .createdAt(requireNonNull(createdAt))
                .build();

        producedEvent.validateState();

        return producedEvent;
    }

    public static ProducedEvent create(
            UUID eventId,
            EventType eventType,
            String payload,
            Instant createdAt
    ) {
        Instant validatedCreatedAt = requireNonNull(createdAt);

        return ProducedEvent.builder()
                .eventId(requireNonNull(eventId))
                .eventType(requireNonNull(eventType))
                .payload(requireNotBlank(payload))
                .status(PENDING)
                .retryCount(0)
                .nextAttemptAt(validatedCreatedAt)
                .createdAt(validatedCreatedAt)
                .build();
    }

    public void startProcessing(Instant startedAt) {
        if (this.status != PENDING) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATUS_TRANSITION);
        }

        Instant validatedStartedAt = requireNonNull(startedAt);
        if (validatedStartedAt.isBefore(this.nextAttemptAt)) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }

        this.status = PROCESSING;
        this.processingStartedAt = validatedStartedAt;
    }

    public void markPublished(Instant publishedAt) {
        if (this.status != PROCESSING) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATUS_TRANSITION);
        }

        Instant validatedPublishedAt = requireNonNull(publishedAt);
        if (validatedPublishedAt.isBefore(this.processingStartedAt)) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }

        this.status = PUBLISHED;
        this.publishedAt = validatedPublishedAt;
    }

    public void scheduleRetry(Instant nextAttemptAt) {
        if (this.status != PROCESSING) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATUS_TRANSITION);
        }

        Instant validatedNextAttemptAt = requireNonNull(nextAttemptAt);

        if (validatedNextAttemptAt.isBefore(this.processingStartedAt)) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }

        this.status = PENDING;
        this.retryCount++;
        this.nextAttemptAt = validatedNextAttemptAt;
        this.processingStartedAt = null;
    }

    public void recoverTimedOutProcessing(Instant nextAttemptAt) {
        scheduleRetry(nextAttemptAt);
    }

    public void markFailed() {
        if (this.status != PROCESSING) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATUS_TRANSITION);
        }

        this.status = FAILED;
        this.retryCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProducedEvent that = (ProducedEvent) o;

        return this.producedEventId != null
                && Objects.equals(this.producedEventId, that.producedEventId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    private void validateState() {
        if (this.nextAttemptAt.isBefore(this.createdAt)) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }

        if (this.status == PENDING) {
            validatePendingState();
        } else if (this.status == PROCESSING) {
            validateProcessingState();
        } else if (this.status == PUBLISHED) {
            validatePublishedState();
        } else if (this.status == FAILED) {
            validateFailedState();
        }
    }

    private void validatePendingState() {
        if (this.processingStartedAt != null || this.publishedAt != null) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }
    }

    private void validateProcessingState() {
        if (this.processingStartedAt == null
                || this.processingStartedAt.isBefore(this.createdAt)
                || this.publishedAt != null) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }
    }

    private void validatePublishedState() {
        if (this.processingStartedAt == null
                || this.processingStartedAt.isBefore(this.createdAt)
                || this.publishedAt == null
                || this.publishedAt.isBefore(this.processingStartedAt)) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }
    }

    private void validateFailedState() {
        if (this.processingStartedAt == null
                || this.processingStartedAt.isBefore(this.createdAt)
                || this.publishedAt != null) {
            throw new BusinessRuleViolationException(INVALID_PRODUCED_EVENT_STATE);
        }
    }
}
