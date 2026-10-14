package net.akive.api.eventprocessing.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.application.provided.EventProcessingTracker;
import net.akive.api.eventprocessing.application.required.ProcessedEventRepository;
import net.akive.api.eventprocessing.domain.EventConsumer;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProcessedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
class EventProcessingTrackingService implements EventProcessingTracker {

    private final ProcessedEventRepository processedEventRepository;

    @Override
    public void markProcessed(UUID eventId, EventType eventType, EventConsumer consumer) {
        processedEventRepository.save(ProcessedEvent.create(eventId, eventType, consumer));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProcessed(UUID eventId, EventConsumer consumer) {
        return processedEventRepository.existsByEventIdAndConsumer(eventId, consumer);
    }
}
