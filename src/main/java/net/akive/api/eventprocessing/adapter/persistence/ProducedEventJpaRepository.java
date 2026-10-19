package net.akive.api.eventprocessing.adapter.persistence;

import net.akive.api.eventprocessing.adapter.persistence.entity.ProducedEventEntity;
import net.akive.api.eventprocessing.domain.EventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

interface ProducedEventJpaRepository extends JpaRepository<ProducedEventEntity, Long> {

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            select producedEvent
            from ProducedEventEntity producedEvent
            where producedEvent.eventType in :eventTypes
              and producedEvent.status = net.akive.api.eventprocessing.domain.ProducedEventStatus.PENDING
              and producedEvent.nextAttemptAt <= :nextAttemptAtCutoff
            order by producedEvent.createdAt asc
    """)
    List<ProducedEventEntity> findPendingByEventTypesAndNextAttemptDueOrderByCreatedAtAsc(
            Collection<EventType> eventTypes,
            Instant nextAttemptAtCutoff,
            Pageable pageable
    );

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            select producedEvent
            from ProducedEventEntity producedEvent
            where producedEvent.eventType in :eventTypes
              and producedEvent.status = net.akive.api.eventprocessing.domain.ProducedEventStatus.PROCESSING
              and producedEvent.processingStartedAt <= :processingStartedAtCutoff
            order by producedEvent.processingStartedAt asc
    """)
    List<ProducedEventEntity> findTimedOutProcessingByEventTypesOrderByProcessingStartedAtAsc(
            Collection<EventType> eventTypes,
            Instant processingStartedAtCutoff,
            Pageable pageable
    );

    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            select producedEvent
            from ProducedEventEntity producedEvent
            where producedEvent.producedEventId = :producedEventId
    """)
    Optional<ProducedEventEntity> findByIdForUpdate(Long producedEventId);

    boolean existsByEventId(UUID eventId);
}
