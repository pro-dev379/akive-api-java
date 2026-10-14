package net.akive.api.eventprocessing.adapter.scheduling;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.aws.ConditionalOnSqsEnabled;
import net.akive.api.eventprocessing.application.provided.ProducedEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnSqsEnabled
class ProducedEventPublishScheduler {

    private final ProducedEventPublisher producedEventPublisher;

    @Scheduled(fixedDelayString = "${event.outbox.publish-delay}")
    void publishPending() {
        producedEventPublisher.publishPending();
    }
}
