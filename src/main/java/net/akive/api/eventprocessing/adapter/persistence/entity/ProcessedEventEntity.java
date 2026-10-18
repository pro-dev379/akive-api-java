package net.akive.api.eventprocessing.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.eventprocessing.domain.EventConsumer;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProcessedEvent;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.type.SqlTypes.BINARY;

@Entity
@Table(
        name = "processed_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-processed_event-event_id-consumer",
                        columnNames = {"event_id", "consumer"}
                )
        }
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class ProcessedEventEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long processedEventId;

    @JdbcTypeCode(BINARY)
    @Column(nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private EventType eventType;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(50)")
    private EventConsumer consumer;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static ProcessedEventEntity from(ProcessedEvent processedEvent) {
        ProcessedEventEntity processedEventEntity = new ProcessedEventEntity();

        processedEventEntity.processedEventId = processedEvent.getProcessedEventId();
        processedEventEntity.eventId = processedEvent.getEventId();
        processedEventEntity.eventType = processedEvent.getEventType();
        processedEventEntity.consumer = processedEvent.getConsumer();
        processedEventEntity.createdAt = processedEvent.getCreatedAt();

        return processedEventEntity;
    }

    public ProcessedEvent to() {
        try {
            return ProcessedEvent.reconstruct(
                    this.processedEventId,
                    this.eventId,
                    this.eventType,
                    this.consumer,
                    this.createdAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct ProcessedEvent: " + this.processedEventId,
                    e
            );
        }
    }
}
