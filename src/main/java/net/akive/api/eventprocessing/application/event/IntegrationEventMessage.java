package net.akive.api.eventprocessing.application.event;

import net.akive.api.eventprocessing.domain.EventType;

import java.time.Instant;
import java.util.UUID;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireNotBlank;

public record IntegrationEventMessage(
        UUID eventId,
        EventType eventType,
        String payload,
        Instant createdAt
) {

    public IntegrationEventMessage {
        requireNonNull(eventId);
        requireNonNull(eventType);
        payload = requireNotBlank(payload);
        requireNonNull(createdAt);
    }
}
