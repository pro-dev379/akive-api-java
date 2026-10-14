package net.akive.api.eventprocessing.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.core.config.aws.ConditionalOnSqsEnabled;
import net.akive.api.eventprocessing.application.provided.ProducedEventPublisher;
import net.akive.api.eventprocessing.application.required.IntegrationEventPublisher;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static net.akive.api.eventprocessing.domain.ProducedEventPolicy.*;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnSqsEnabled
class ProducedEventPublishService implements ProducedEventPublisher {

    private final IntegrationEventPublisher integrationEventPublisher;

    private final ProducedEventClaimService producedEventClaimService;
    private final CurrentTimeProvider currentTimeProvider;

    @Override
    public void publishPending() {
        List<ProducedEvent> producedEvents = producedEventClaimService.claim(
                integrationEventPublisher.supportedEventTypes(),
                currentTimeProvider.now(),
                PROCESSING_TIMEOUT,
                PUBLISH_ATTEMPT_MAX_COUNT,
                PUBLISH_BATCH_SIZE
        );

        producedEvents.forEach(this::publish);
    }

    private void publish(ProducedEvent producedEvent) {
        try {
            integrationEventPublisher.publish(producedEvent);
            producedEventClaimService.markPublished(producedEvent.getProducedEventId(), currentTimeProvider.now());
        } catch (RuntimeException e) {
            handlePublishFailure(producedEvent, e);
        }
    }

    private void handlePublishFailure(ProducedEvent producedEvent, RuntimeException error) {
        Instant failedAt = currentTimeProvider.now();
        Instant nextAttemptAt = failedAt.plus(calculateRetryDelay(producedEvent.getRetryCount()));

        try {
            producedEventClaimService.handlePublishFailure(
                    producedEvent.getProducedEventId(),
                    failedAt,
                    nextAttemptAt,
                    PUBLISH_ATTEMPT_MAX_COUNT
            );
        } catch (RuntimeException stateUpdateError) {
            stateUpdateError.addSuppressed(error);
            throw stateUpdateError;
        }

        log.warn(
                "Failed to publish integration event: eventId={}, eventType={}, attempt={}",
                producedEvent.getEventId(),
                producedEvent.getEventType(),
                producedEvent.getRetryCount() + 1,
                error
        );
    }

    private Duration calculateRetryDelay(int retryCount) {
        long multiplier = 1L << Math.min(retryCount, 30);
        Duration retryDelay = INITIAL_RETRY_DELAY.multipliedBy(multiplier);

        return retryDelay.compareTo(MAX_RETRY_DELAY) > 0
                ? MAX_RETRY_DELAY
                : retryDelay;
    }
}
