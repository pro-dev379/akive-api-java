package net.akive.api.eventprocessing.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.application.event.IntegrationEvent;
import net.akive.api.eventprocessing.application.provided.IntegrationEventRecorder;
import net.akive.api.eventprocessing.application.required.IntegrationEventSerializer;
import net.akive.api.eventprocessing.application.required.ProducedEventRepository;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class IntegrationEventRecordService implements IntegrationEventRecorder {

    private final IntegrationEventSerializer integrationEventSerializer;
    private final ProducedEventRepository producedEventRepository;

    @Override
    public void record(IntegrationEvent event) {
        if (producedEventRepository.existsByEventId(event.eventId())) {
            return;
        }

        String payload = integrationEventSerializer.serialize(event);

        producedEventRepository.save(ProducedEvent.create(
                event.eventId(),
                event.eventType(),
                payload,
                event.createdAt()
        ));
    }
}
