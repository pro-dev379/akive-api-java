package net.akive.api.eventprocessing.adapter.persistence;

import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.adapter.persistence.entity.ProducedEventEntity;
import net.akive.api.eventprocessing.application.required.ProducedEventRepository;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ProducedEventJpaAdapter implements ProducedEventRepository {

    private final ProducedEventJpaRepository producedEventJpaRepository;

    @Override
    public ProducedEvent save(ProducedEvent producedEvent) {
        return producedEventJpaRepository
                .save(ProducedEventEntity.from(producedEvent))
                .to();
    }

    @Override
    public List<ProducedEvent> findPendingForUpdate(
            Collection<EventType> eventTypes,
            Instant nextAttemptAtCutoff,
            int limit
    ) {
        return producedEventJpaRepository
                .findPendingByEventTypesAndNextAttemptDueOrderByCreatedAtAsc(
                        eventTypes,
                        nextAttemptAtCutoff,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(ProducedEventEntity::to)
                .toList();
    }

    @Override
    public List<ProducedEvent> findTimedOutProcessingForUpdate(
            Collection<EventType> eventTypes,
            Instant processingStartedAtCutoff,
            int limit
    ) {
        return producedEventJpaRepository
                .findTimedOutProcessingByEventTypesOrderByProcessingStartedAtAsc(
                        eventTypes,
                        processingStartedAtCutoff,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(ProducedEventEntity::to)
                .toList();
    }

    @Override
    public Optional<ProducedEvent> findByIdForUpdate(Long producedEventId) {
        return producedEventJpaRepository
                .findByIdForUpdate(producedEventId)
                .map(ProducedEventEntity::to);
    }

    @Override
    public boolean existsByEventId(UUID eventId) {
        return producedEventJpaRepository.existsByEventId(eventId);
    }
}
