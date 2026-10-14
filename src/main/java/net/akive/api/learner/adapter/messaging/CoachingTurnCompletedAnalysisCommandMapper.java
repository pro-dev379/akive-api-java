package net.akive.api.learner.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.application.event.CoachingTurnCompletedEvent;
import net.akive.api.coaching.application.provided.CoachingTurnFinder;
import net.akive.api.coaching.application.provided.dto.CoachingTurnFindQuery;
import net.akive.api.coaching.application.provided.dto.CoachingTurnResult;
import net.akive.api.core.config.aws.ConditionalOnSqsEnabled;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.learner.application.exception.LearnerInsightMessageException;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisMaterialData;
import net.akive.api.learner.application.provided.dto.LearnerInsightEvidenceSourceData;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.EVIDENCE;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.REFERENCE;
import static net.akive.api.learner.domain.EvidenceSourceType.COACHING_MESSAGE;

@Component
@RequiredArgsConstructor
@ConditionalOnSqsEnabled
class CoachingTurnCompletedAnalysisCommandMapper implements LearnerInsightAnalysisCommandMapper {

    private final CoachingTurnFinder coachingTurnFinder;
    private final ObjectMapper objectMapper;

    @Override
    public EventType supportedEventType() {
        return COACHING_TURN_DONE;
    }

    @Override
    public LearnerInsightAnalysisCommand map(IntegrationEventMessage message) {
        CoachingTurnCompletedEvent event = deserialize(message.payload());

        validateEnvelope(message, event);

        CoachingTurnResult coachingTurn = findCoachingTurnOrThrow(event);

        return new LearnerInsightAnalysisCommand(
                event.eventId(),
                event.eventType(),
                event.userId(),
                new LearnerInsightEvidenceSourceData(
                        COACHING_MESSAGE,
                        event.userMessageId(),
                        event.completedAt()
                ),
                List.of(
                        new LearnerInsightAnalysisMaterialData(EVIDENCE, coachingTurn.userMessage()),
                        new LearnerInsightAnalysisMaterialData(REFERENCE, coachingTurn.assistantMessage())
                )
        );
    }

    private CoachingTurnCompletedEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, CoachingTurnCompletedEvent.class);
        } catch (JsonProcessingException e) {
            throw new LearnerInsightMessageException(
                    "Failed to deserialize a coaching turn completed event.",
                    e
            );
        }
    }

    private void validateEnvelope(
            IntegrationEventMessage message,
            CoachingTurnCompletedEvent event
    ) {
        if (!message.eventId().equals(event.eventId()) || message.eventType() != event.eventType()) {
            throw new LearnerInsightMessageException(
                    "Integration event envelope does not match its payload: " + message.eventId()
            );
        }
    }

    private CoachingTurnResult findCoachingTurnOrThrow(CoachingTurnCompletedEvent event) {
        return coachingTurnFinder.find(new CoachingTurnFindQuery(
                event.userId(),
                event.coachingConversationId(),
                event.userMessageId(),
                event.assistantMessageId()
        )).orElseThrow(() -> new LearnerInsightMessageException(
                "Coaching turn referenced by the integration event could not be found: " + event.eventId()
        ));
    }
}
