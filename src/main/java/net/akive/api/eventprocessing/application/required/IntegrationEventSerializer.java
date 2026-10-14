package net.akive.api.eventprocessing.application.required;

import net.akive.api.eventprocessing.application.event.IntegrationEvent;

public interface IntegrationEventSerializer {

    String serialize(IntegrationEvent event);
}
