package net.akive.api.eventprocessing.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.adapter.persistence.entity.ProcessedEventEntity;
import net.akive.api.eventprocessing.application.required.ProcessedEventRepository;
import net.akive.api.eventprocessing.domain.EventConsumer;
import net.akive.api.eventprocessing.domain.ProcessedEvent;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ProcessedEventJpaAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository processedEventJpaRepository;

    @Override
    public ProcessedEvent save(ProcessedEvent processedEvent) {
        return processedEventJpaRepository
                .save(ProcessedEventEntity.from(processedEvent))
                .to();
    }

    @Override
    public boolean existsByEventIdAndConsumer(UUID eventId, EventConsumer consumer) {
        return processedEventJpaRepository.existsByEventIdAndConsumer(eventId, consumer);
    }
}
