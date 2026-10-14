package net.akive.api.eventprocessing.application.required;

import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEvent;

import java.util.Set;

public interface IntegrationEventPublisher {

    Set<EventType> supportedEventTypes();

    void publish(ProducedEvent producedEvent);
}
