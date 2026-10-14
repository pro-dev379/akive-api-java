package net.akive.api.eventprocessing.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.aws.ConditionalOnSqsEnabled;
import net.akive.api.core.config.aws.EventSqsProperties;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.eventprocessing.application.exception.IntegrationEventPublicationException;
import net.akive.api.eventprocessing.application.required.IntegrationEventPublisher;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnSqsEnabled
class SqsIntegrationEventPublisher implements IntegrationEventPublisher {

    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;
    private final EventSqsProperties eventSqsProperties;

    @Override
    public Set<EventType> supportedEventTypes() {
        return Set.of(EventType.values());
    }

    @Override
    public void publish(ProducedEvent producedEvent) {
        try {
            SqsDestination destination = resolveDestination(producedEvent);
            IntegrationEventMessage message = new IntegrationEventMessage(
                    producedEvent.getEventId(),
                    producedEvent.getEventType(),
                    producedEvent.getPayload(),
                    producedEvent.getCreatedAt()
            );

            sqsTemplate.send(to -> to
                    .queue(destination.queueName())
                    .payload(message)
                    .messageGroupId(destination.messageGroupId())
                    .messageDeduplicationId(producedEvent.getEventId().toString())
            );
        } catch (RuntimeException | JsonProcessingException e) {
            throw new IntegrationEventPublicationException(
                    "Failed to publish integration event to SQS: " + producedEvent.getEventId(),
                    e
            );
        }
    }

    private SqsDestination resolveDestination(ProducedEvent producedEvent) throws JsonProcessingException {
        return switch (producedEvent.getEventType()) {
            case COACHING_TURN_DONE -> new SqsDestination(
                    eventSqsProperties.learnerInsightAnalysis().queueName(),
                    extractGroupId(producedEvent.getPayload(), "userId")
            );
            case INSIGHT_CHANGED -> new SqsDestination(
                    eventSqsProperties.learnerInsightEmbedding().queueName(),
                    extractGroupId(producedEvent.getPayload(), "learnerInsightId")
            );
        };
    }

    private String extractGroupId(String payload, String fieldName) throws JsonProcessingException {
        JsonNode groupId = objectMapper.readTree(payload).get(fieldName);

        if (groupId == null || !groupId.canConvertToLong()) {
            throw new IntegrationEventPublicationException(
                    "Integration event payload does not contain a valid " + fieldName + "."
            );
        }

        return Long.toString(groupId.longValue());
    }

    private record SqsDestination(String queueName, String messageGroupId) {
    }
}
