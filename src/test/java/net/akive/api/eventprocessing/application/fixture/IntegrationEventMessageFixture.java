package net.akive.api.eventprocessing.application.fixture;

import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.eventprocessing.domain.EventType;

import java.time.Instant;
import java.util.UUID;

public final class IntegrationEventMessageFixture {

    public static final String INTEGRATION_EVENT_PAYLOAD =
            "{\"eventId\":\"0199a8e2-7c31-7abc-8123-456789abcdef\"}";
    public static final Instant INTEGRATION_EVENT_CREATED_AT =
            Instant.parse("2026-10-17T00:00:00Z");

    private IntegrationEventMessageFixture() {
    }

    public static IntegrationEventMessage createIntegrationEventMessage(
            UUID eventId,
            EventType eventType
    ) {
        return createIntegrationEventMessage(eventId, eventType, INTEGRATION_EVENT_PAYLOAD);
    }

    public static IntegrationEventMessage createIntegrationEventMessage(
            UUID eventId,
            EventType eventType,
            String payload
    ) {
        return new IntegrationEventMessage(
                eventId,
                eventType,
                payload,
                INTEGRATION_EVENT_CREATED_AT
        );
    }
}
