package net.akive.api.eventprocessing.application.event;

import net.akive.api.eventprocessing.domain.EventType;

import java.time.Instant;
import java.util.UUID;

public interface IntegrationEvent {

    UUID eventId();

    EventType eventType();

    Instant createdAt();
}
