package net.akive.api.eventprocessing.application.required;

import net.akive.api.eventprocessing.domain.ProducedEvent;
import net.akive.api.eventprocessing.domain.EventType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProducedEventRepository {

    ProducedEvent save(ProducedEvent producedEvent);

    List<ProducedEvent> findPendingForUpdate(
            Collection<EventType> eventTypes,
            Instant nextAttemptAtCutoff,
            int limit
    );

    List<ProducedEvent> findTimedOutProcessingForUpdate(
            Collection<EventType> eventTypes,
            Instant processingStartedAtCutoff,
            int limit
    );

    Optional<ProducedEvent> findByIdForUpdate(Long producedEventId);

    boolean existsByEventId(UUID eventId);
}
