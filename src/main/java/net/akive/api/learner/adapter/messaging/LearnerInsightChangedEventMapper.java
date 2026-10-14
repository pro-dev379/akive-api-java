package net.akive.api.learner.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.aws.ConditionalOnSqsEnabled;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.learner.application.event.LearnerInsightChangedEvent;
import net.akive.api.learner.application.exception.LearnerInsightMessageException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnSqsEnabled
class LearnerInsightChangedEventMapper {

    private final ObjectMapper objectMapper;

    LearnerInsightChangedEvent map(IntegrationEventMessage message) {
        LearnerInsightChangedEvent event = deserialize(message.payload());

        if (!message.eventId().equals(event.eventId()) || message.eventType() != event.eventType()) {
            throw new LearnerInsightMessageException(
                    "Integration event envelope does not match its payload: " + message.eventId()
            );
        }

        return event;
    }

    private LearnerInsightChangedEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, LearnerInsightChangedEvent.class);
        } catch (JsonProcessingException e) {
            throw new LearnerInsightMessageException(
                    "Failed to deserialize a learner insight changed event.",
                    e
            );
        }
    }
}
