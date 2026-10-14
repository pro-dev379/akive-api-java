package net.akive.api.eventprocessing.application.required;

import net.akive.api.eventprocessing.domain.EventConsumer;
import net.akive.api.eventprocessing.domain.ProcessedEvent;

import java.util.UUID;

public interface ProcessedEventRepository {

    ProcessedEvent save(ProcessedEvent processedEvent);

    boolean existsByEventIdAndConsumer(UUID eventId, EventConsumer consumer);
}
