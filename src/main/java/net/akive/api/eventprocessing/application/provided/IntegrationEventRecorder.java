package net.akive.api.eventprocessing.application.provided;

import net.akive.api.eventprocessing.application.event.IntegrationEvent;

public interface IntegrationEventRecorder {

    void record(IntegrationEvent event);
}
