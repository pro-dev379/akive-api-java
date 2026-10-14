package net.akive.api.eventprocessing.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.application.exception.ProducedEventNotFoundException;
import net.akive.api.eventprocessing.application.required.ProducedEventRepository;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
class ProducedEventClaimService {

    private final ProducedEventRepository producedEventRepository;

    public List<ProducedEvent> claim(
            Set<EventType> supportedEventTypes,
            Instant attemptedAt,
            Duration processingTimeout,
            int maxPublishAttempts,
            int limit
    ) {
        recoverTimedOutProcessing(
                supportedEventTypes,
                attemptedAt,
                processingTimeout,
                maxPublishAttempts,
                limit
        );

        List<ProducedEvent> producedEvents = producedEventRepository.findPendingForUpdate(
                supportedEventTypes,
                attemptedAt,
                limit
        );

        producedEvents.forEach(producedEvent -> {
            producedEvent.startProcessing(attemptedAt);
            producedEventRepository.save(producedEvent);
        });

        return producedEvents;
    }

    public void markPublished(Long producedEventId, Instant publishedAt) {
        ProducedEvent producedEvent = findByIdForUpdateOrThrow(producedEventId);

        producedEvent.markPublished(publishedAt);
        producedEventRepository.save(producedEvent);
    }

    public void handlePublishFailure(
            Long producedEventId,
            Instant failedAt,
            Instant nextAttemptAt,
            int maxPublishAttempts
    ) {
        ProducedEvent producedEvent = findByIdForUpdateOrThrow(producedEventId);

        if (producedEvent.getRetryCount() + 1 >= maxPublishAttempts) {
            producedEvent.markFailed();
        } else {
            producedEvent.scheduleRetry(nextAttemptAt.isBefore(failedAt) ? failedAt : nextAttemptAt);
        }

        producedEventRepository.save(producedEvent);
    }

    private void recoverTimedOutProcessing(
            Set<EventType> supportedEventTypes,
            Instant attemptedAt,
            Duration processingTimeout,
            int maxPublishAttempts,
            int limit
    ) {
        Instant processingStartedAtCutoff = attemptedAt.minus(processingTimeout);

        producedEventRepository.findTimedOutProcessingForUpdate(
                supportedEventTypes,
                processingStartedAtCutoff,
                limit
        ).forEach(producedEvent -> {
            if (producedEvent.getRetryCount() + 1 >= maxPublishAttempts) {
                producedEvent.markFailed();
            } else {
                producedEvent.recoverTimedOutProcessing(attemptedAt);
            }

            producedEventRepository.save(producedEvent);
        });
    }

    private ProducedEvent findByIdForUpdateOrThrow(Long producedEventId) {
        return producedEventRepository.findByIdForUpdate(producedEventId)
                .orElseThrow(() -> new ProducedEventNotFoundException(producedEventId));
    }
}
