package net.akive.api.eventprocessing.adapter.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.internal.InvalidPersistedDataException;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import net.akive.api.eventprocessing.domain.ProducedEventStatus;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.Instant;
import java.util.UUID;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.hibernate.type.SqlTypes.BINARY;
import static org.hibernate.type.SqlTypes.JSON;

@Entity
@Table(
        name = "produced_event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux-produced_event-event_id",
                        columnNames = "event_id"
                )
        }
)
@Getter
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PROTECTED)
public class ProducedEventEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long producedEventId;

    @JdbcTypeCode(BINARY)
    @Column(nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Enumerated(STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "VARCHAR(20)")
    private EventType eventType;

    @JdbcTypeCode(JSON)
    @Column(nullable = false, updatable = false, columnDefinition = "JSON")
    private String payload;

    @Enumerated(STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private ProducedEventStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private Instant nextAttemptAt;

    private Instant processingStartedAt;

    private Instant publishedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static ProducedEventEntity from(ProducedEvent producedEvent) {
        return new ProducedEventEntity(
                producedEvent.getProducedEventId(),
                producedEvent.getEventId(),
                producedEvent.getEventType(),
                producedEvent.getPayload(),
                producedEvent.getStatus(),
                producedEvent.getRetryCount(),
                producedEvent.getNextAttemptAt(),
                producedEvent.getProcessingStartedAt(),
                producedEvent.getPublishedAt(),
                producedEvent.getCreatedAt()
        );
    }

    public ProducedEvent to() {
        try {
            return ProducedEvent.reconstruct(
                    this.producedEventId,
                    this.eventId,
                    this.eventType,
                    this.payload,
                    this.status,
                    this.retryCount,
                    this.nextAttemptAt,
                    this.processingStartedAt,
                    this.publishedAt,
                    this.createdAt
            );
        } catch (BusinessRuleViolationException e) {
            throw new InvalidPersistedDataException(
                    "Failed to reconstruct ProducedEvent: " + this.producedEventId,
                    e
            );
        }
    }
}
