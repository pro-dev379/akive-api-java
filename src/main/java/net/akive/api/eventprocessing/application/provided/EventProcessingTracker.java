package net.akive.api.eventprocessing.application.provided;

import net.akive.api.eventprocessing.domain.EventConsumer;
import net.akive.api.eventprocessing.domain.EventType;

import java.util.UUID;

public interface EventProcessingTracker {

    void markProcessed(UUID eventId, EventType eventType, EventConsumer consumer);

    boolean isProcessed(UUID eventId, EventConsumer consumer);
}
