package net.akive.api.eventprocessing.adapter.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.application.event.IntegrationEvent;
import net.akive.api.eventprocessing.application.exception.IntegrationEventSerializationException;
import net.akive.api.eventprocessing.application.required.IntegrationEventSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class JacksonIntegrationEventSerializer implements IntegrationEventSerializer {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(IntegrationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IntegrationEventSerializationException(
                    "Failed to serialize integration event: " + event.eventId(),
                    e
            );
        }
    }
}
