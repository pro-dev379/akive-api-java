package net.akive.api.eventprocessing.domain.fixture;

import net.akive.api.eventprocessing.domain.ProducedEvent;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEventStatus;

import java.time.Instant;
import java.util.UUID;

import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.ProducedEventStatus.PENDING;
import static net.akive.api.eventprocessing.domain.ProducedEventStatus.PROCESSING;

public class ProducedEventFixture {

    public static final Long PRODUCED_EVENT_ID = 1L;
    public static final Long ANOTHER_PRODUCED_EVENT_ID = 2L;
    public static final UUID FIRST_EVENT_ID = UUID.fromString("0199a8e2-7c31-7abc-8123-456789abcdef");
    public static final UUID SECOND_EVENT_ID = UUID.fromString("08609705-5187-4956-872d-e78834230681");
    public static final UUID THIRD_EVENT_ID = UUID.fromString("d032ed06-de8f-4f49-9489-535973ed3126");
    public static final UUID FOURTH_EVENT_ID = UUID.fromString("d3d2e508-2f66-4136-ac9e-7a7d266573cb");
    public static final UUID UNKNOWN_EVENT_ID = UUID.fromString("3fd8e730-d060-401d-bc47-a0b67950b9a9");
    public static final String PAYLOAD = "{\"coachingConversationId\":1}";
    public static final int RETRY_COUNT = 1;
    public static final Instant CREATED_AT = Instant.parse("2026-10-16T00:00:00Z");
    public static final Instant PROCESSING_STARTED_AT = CREATED_AT.plusSeconds(1);
    public static final Instant NEXT_ATTEMPT_AT = PROCESSING_STARTED_AT.plusSeconds(5);
    public static final Instant PUBLISHED_AT = PROCESSING_STARTED_AT.plusSeconds(2);

    private ProducedEventFixture() {
    }

    public static ProducedEvent createProducedEvent() {
        return ProducedEvent.create(FIRST_EVENT_ID, COACHING_TURN_DONE, PAYLOAD, CREATED_AT);
    }

    public static ProducedEvent createProcessingProducedEvent(
            UUID eventId,
            EventType eventType,
            Instant processingStartedAt
    ) {
        ProducedEvent producedEvent = ProducedEvent.create(
                eventId,
                eventType,
                PAYLOAD,
                CREATED_AT
        );
        producedEvent.startProcessing(processingStartedAt);

        return producedEvent;
    }

    public static ProducedEvent reconstructProcessingProducedEvent(int retryCount) {
        return reconstructProcessingProducedEvent(COACHING_TURN_DONE, retryCount);
    }

    public static ProducedEvent reconstructProcessingProducedEvent(
            EventType eventType,
            int retryCount
    ) {
        return ProducedEvent.reconstruct(
                PRODUCED_EVENT_ID,
                FIRST_EVENT_ID,
                eventType,
                PAYLOAD,
                PROCESSING,
                retryCount,
                CREATED_AT,
                PROCESSING_STARTED_AT,
                null,
                CREATED_AT
        );
    }

    public static ProducedEvent reconstructProducedEvent() {
        return reconstructProducedEvent(
                PRODUCED_EVENT_ID,
                PENDING,
                RETRY_COUNT,
                NEXT_ATTEMPT_AT,
                null,
                null
        );
    }

    public static ProducedEvent reconstructProducedEvent(
            Long producedEventId,
            ProducedEventStatus status,
            int retryCount,
            Instant nextAttemptAt,
            Instant processingStartedAt,
            Instant publishedAt
    ) {
        return ProducedEvent.reconstruct(
                producedEventId,
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                PAYLOAD,
                status,
                retryCount,
                nextAttemptAt,
                processingStartedAt,
                publishedAt,
                CREATED_AT
        );
    }
}
