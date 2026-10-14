package net.akive.api.eventprocessing.adapter.persistence;

import net.akive.api.eventprocessing.adapter.persistence.entity.ProcessedEventEntity;
import net.akive.api.eventprocessing.domain.EventConsumer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, Long> {

    boolean existsByEventIdAndConsumer(UUID eventId, EventConsumer consumer);
}
