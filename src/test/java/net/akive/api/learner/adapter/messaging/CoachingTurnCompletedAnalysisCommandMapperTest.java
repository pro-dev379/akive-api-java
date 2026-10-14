package net.akive.api.learner.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.coaching.application.event.CoachingTurnCompletedEvent;
import net.akive.api.coaching.application.provided.CoachingTurnFinder;
import net.akive.api.coaching.application.provided.dto.CoachingTurnFindQuery;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.learner.application.exception.LearnerInsightMessageException;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.EVENT_ID;
import static net.akive.api.coaching.application.fixture.CoachingTurnFixture.*;
import static net.akive.api.coaching.application.service.fixture.CoachingTurnQueryFixture.createCoachingTurnFindQuery;
import static net.akive.api.eventprocessing.application.fixture.IntegrationEventMessageFixture.INTEGRATION_EVENT_PAYLOAD;
import static net.akive.api.eventprocessing.application.fixture.IntegrationEventMessageFixture.createIntegrationEventMessage;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.EventType.INSIGHT_CHANGED;
import static net.akive.api.learner.application.fixture.LearnerInsightAnalysisFixture.createLearnerInsightAnalysisCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CoachingTurnCompletedAnalysisCommandMapperTest {

    @Mock
    CoachingTurnFinder coachingTurnFinder;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    CoachingTurnCompletedAnalysisCommandMapper commandMapper;

    @Test
    void Command_DTO_매핑_대상의_이벤트_타입을_반환한다() {
        assertThat(commandMapper.supportedEventType()).isEqualTo(COACHING_TURN_DONE);
    }

    @Test
    void 회원_메시지에_대한_AI_코치_응답_완료_이벤트를_학습자_인사이트_분석을_위한_Command_DTO로_변환한다()
            throws JsonProcessingException {
        CoachingTurnCompletedEvent event = createCoachingTurnCompletedEvent(EVENT_ID);
        IntegrationEventMessage message = createIntegrationEventMessage(EVENT_ID, COACHING_TURN_DONE);
        CoachingTurnFindQuery findQuery = createCoachingTurnFindQuery();
        given(objectMapper.readValue(INTEGRATION_EVENT_PAYLOAD, CoachingTurnCompletedEvent.class))
                .willReturn(event);
        given(coachingTurnFinder.find(findQuery)).willReturn(Optional.of(createCoachingTurnResult()));

        LearnerInsightAnalysisCommand result = commandMapper.map(message);

        assertThat(result).isEqualTo(createLearnerInsightAnalysisCommand(EVENT_ID, COACHING_TURN_DONE));
        then(coachingTurnFinder).should().find(findQuery);
    }

    @Test
    void 회원_메시지에_대한_AI_코치_응답_완료_이벤트를_역직렬화할_수_없으면_예외를_던진다() throws JsonProcessingException {
        IntegrationEventMessage message = createIntegrationEventMessage(EVENT_ID, COACHING_TURN_DONE);
        JsonProcessingException deserializationException = mock(JsonProcessingException.class);
        given(objectMapper.readValue(INTEGRATION_EVENT_PAYLOAD, CoachingTurnCompletedEvent.class))
                .willThrow(deserializationException);

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> commandMapper.map(message))
                .withMessage("Failed to deserialize a coaching turn completed event.")
                .withCause(deserializationException);
    }

    @Test
    void 통합_이벤트_메시지_식별자와_회원_메시지에_대한_AI_코치_응답_완료_이벤트_식별자가_다르면_예외를_던진다()
            throws JsonProcessingException {
        CoachingTurnCompletedEvent event = createCoachingTurnCompletedEvent(EVENT_ID);
        IntegrationEventMessage message = createIntegrationEventMessage(ANOTHER_EVENT_ID, COACHING_TURN_DONE);
        given(objectMapper.readValue(INTEGRATION_EVENT_PAYLOAD, CoachingTurnCompletedEvent.class))
                .willReturn(event);

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> commandMapper.map(message))
                .withMessage("Integration event envelope does not match its payload: " + ANOTHER_EVENT_ID);
        then(coachingTurnFinder).should(never()).find(any(CoachingTurnFindQuery.class));
    }

    @Test
    void 통합_이벤트_메시지_타입과_회원_메시지에_대한_AI_코치_응답_완료_이벤트_타입이_다르면_예외를_던진다()
            throws JsonProcessingException {
        CoachingTurnCompletedEvent eventWithCoachingTurnDoneType = createCoachingTurnCompletedEvent(EVENT_ID);
        IntegrationEventMessage messageWithInsightChangedType =
                createIntegrationEventMessage(EVENT_ID, INSIGHT_CHANGED);
        given(objectMapper.readValue(INTEGRATION_EVENT_PAYLOAD, CoachingTurnCompletedEvent.class))
                .willReturn(eventWithCoachingTurnDoneType);

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> commandMapper.map(messageWithInsightChangedType))
                .withMessage("Integration event envelope does not match its payload: " + EVENT_ID);
        then(coachingTurnFinder).should(never()).find(any(CoachingTurnFindQuery.class));
    }

    @Test
    void 통합_이벤트가_참조하는_회원_메시지에_대한_AI_코치_응답_완료_이벤트를_찾을_수_없으면_예외를_던진다()
            throws JsonProcessingException {
        CoachingTurnCompletedEvent event = createCoachingTurnCompletedEvent(EVENT_ID);
        IntegrationEventMessage message = createIntegrationEventMessage(EVENT_ID, COACHING_TURN_DONE);
        CoachingTurnFindQuery findQuery = createCoachingTurnFindQuery();
        given(objectMapper.readValue(INTEGRATION_EVENT_PAYLOAD, CoachingTurnCompletedEvent.class))
                .willReturn(event);
        given(coachingTurnFinder.find(findQuery)).willReturn(Optional.empty());

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> commandMapper.map(message))
                .withMessage(
                        "Coaching turn referenced by the integration event could not be found: "
                                + EVENT_ID
                );
    }
}
