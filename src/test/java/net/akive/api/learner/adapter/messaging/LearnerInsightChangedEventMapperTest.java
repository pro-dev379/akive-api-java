package net.akive.api.learner.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.learner.application.event.LearnerInsightChangedEvent;
import net.akive.api.learner.application.exception.LearnerInsightMessageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.eventprocessing.application.fixture.IntegrationEventMessageFixture.INTEGRATION_EVENT_PAYLOAD;
import static net.akive.api.eventprocessing.application.fixture.IntegrationEventMessageFixture.createIntegrationEventMessage;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.EventType.INSIGHT_CHANGED;
import static net.akive.api.learner.application.fixture.LearnerInsightEventFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LearnerInsightChangedEventMapperTest {

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    LearnerInsightChangedEventMapper eventMapper;

    @Test
    void 통합_이벤트_메시지를_학습자_인사이트_변경_이벤트로_변환한다() throws JsonProcessingException {
        LearnerInsightChangedEvent event = createLearnerInsightChangedEvent(LEARNER_INSIGHT_CHANGED_EVENT_ID);
        IntegrationEventMessage message = createIntegrationEventMessage(
                LEARNER_INSIGHT_CHANGED_EVENT_ID,
                INSIGHT_CHANGED
        );
        given(objectMapper.readValue(
                INTEGRATION_EVENT_PAYLOAD,
                LearnerInsightChangedEvent.class
        )).willReturn(event);

        LearnerInsightChangedEvent result = eventMapper.map(message);

        assertThat(result).isEqualTo(event);
    }

    @Test
    void 학습자_인사이트_변경_이벤트를_역직렬화할_수_없으면_예외를_던진다() throws JsonProcessingException {
        IntegrationEventMessage message = createIntegrationEventMessage(
                LEARNER_INSIGHT_CHANGED_EVENT_ID,
                INSIGHT_CHANGED
        );
        JsonProcessingException deserializationException = mock(JsonProcessingException.class);
        given(objectMapper.readValue(
                INTEGRATION_EVENT_PAYLOAD,
                LearnerInsightChangedEvent.class
        )).willThrow(deserializationException);

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> eventMapper.map(message))
                .withMessage("Failed to deserialize a learner insight changed event.")
                .withCause(deserializationException);
    }

    @Test
    void 통합_이벤트_메시지_식별자와_학습자_인사이트_변경_이벤트_식별자가_다르면_예외를_던진다() throws JsonProcessingException {
        LearnerInsightChangedEvent event =
                createLearnerInsightChangedEvent(LEARNER_INSIGHT_CHANGED_EVENT_ID);
        IntegrationEventMessage message = createIntegrationEventMessage(
                ANOTHER_LEARNER_INSIGHT_CHANGED_EVENT_ID,
                INSIGHT_CHANGED
        );
        given(objectMapper.readValue(
                INTEGRATION_EVENT_PAYLOAD,
                LearnerInsightChangedEvent.class
        )).willReturn(event);

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> eventMapper.map(message))
                .withMessage(
                        "Integration event envelope does not match its payload: "
                                + ANOTHER_LEARNER_INSIGHT_CHANGED_EVENT_ID
                );
    }

    @Test
    void 통합_이벤트_메시지_타입과_학습자_인사이트_변경_이벤트_타입이_다르면_예외를_던진다() throws JsonProcessingException {
        LearnerInsightChangedEvent eventWithInsightChangedType =
                createLearnerInsightChangedEvent(LEARNER_INSIGHT_CHANGED_EVENT_ID);
        IntegrationEventMessage messageWithCoachingTurnDoneType = createIntegrationEventMessage(
                LEARNER_INSIGHT_CHANGED_EVENT_ID,
                COACHING_TURN_DONE
        );
        given(objectMapper.readValue(
                INTEGRATION_EVENT_PAYLOAD,
                LearnerInsightChangedEvent.class
        )).willReturn(eventWithInsightChangedType);

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> eventMapper.map(messageWithCoachingTurnDoneType))
                .withMessage(
                        "Integration event envelope does not match its payload: "
                                + LEARNER_INSIGHT_CHANGED_EVENT_ID
                );
    }
}
