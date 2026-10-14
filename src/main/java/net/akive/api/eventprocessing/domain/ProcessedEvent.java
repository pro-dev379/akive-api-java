package net.akive.api.eventprocessing.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessedEvent {

    private Long processedEventId;

    private UUID eventId;

    private EventType eventType;

    private EventConsumer consumer;

    private Instant createdAt;

    public static ProcessedEvent create(
            UUID eventId,
            EventType eventType,
            EventConsumer consumer
    ) {
        return ProcessedEvent.builder()
                .eventId(requireNonNull(eventId))
                .eventType(requireNonNull(eventType))
                .consumer(requireNonNull(consumer))
                .build();
    }

    public static ProcessedEvent reconstruct(
            Long processedEventId,
            UUID eventId,
            EventType eventType,
            EventConsumer consumer,
            Instant createdAt
    ) {
        return ProcessedEvent.builder()
                .processedEventId(requirePositive(processedEventId))
                .eventId(requireNonNull(eventId))
                .eventType(requireNonNull(eventType))
                .consumer(requireNonNull(consumer))
                .createdAt(requireNonNull(createdAt))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcessedEvent that = (ProcessedEvent) o;

        return this.processedEventId != null
                && Objects.equals(this.processedEventId, that.processedEventId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
