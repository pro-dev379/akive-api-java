package net.akive.api.learner.adapter.messaging;

import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.learner.application.exception.LearnerInsightMessageException;
import net.akive.api.learner.application.provided.LearnerInsightAnalyzer;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.EVENT_ID;
import static net.akive.api.eventprocessing.application.fixture.IntegrationEventMessageFixture.createIntegrationEventMessage;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.EventType.INSIGHT_CHANGED;
import static net.akive.api.learner.application.fixture.LearnerInsightAnalysisFixture.createLearnerInsightAnalysisCommand;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LearnerInsightAnalysisSqsListenerTest {

    @Mock
    LearnerInsightAnalysisCommandMapper commandMapper;

    @Mock
    LearnerInsightAnalysisCommandMapper anotherCommandMapper;

    @Mock
    LearnerInsightAnalyzer learnerInsightAnalyzer;

    LearnerInsightAnalysisSqsListener sqsListener;

    @BeforeEach
    void setUp() {
        given(commandMapper.supportedEventType()).willReturn(COACHING_TURN_DONE);
        sqsListener = new LearnerInsightAnalysisSqsListener(List.of(commandMapper), learnerInsightAnalyzer);
    }

    @Test
    void 통합_이벤트_타입에_맞는_Mapper로_학습자_인사이트_분석_Command_DTO를_생성하고_분석한다() {
        IntegrationEventMessage message = createIntegrationEventMessage(EVENT_ID, COACHING_TURN_DONE);
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        given(commandMapper.map(message)).willReturn(command);

        sqsListener.consume(message);

        then(commandMapper).should().map(message);
        then(learnerInsightAnalyzer).should().analyze(command);
    }

    @Test
    void 통합_이벤트_타입을_지원하는_Command_DTO_Mapper가_없으면_예외를_던진다() {
        IntegrationEventMessage message = createIntegrationEventMessage(EVENT_ID, INSIGHT_CHANGED);

        assertThatExceptionOfType(LearnerInsightMessageException.class)
                .isThrownBy(() -> sqsListener.consume(message))
                .withMessage("Learner insight analysis command mapper is not configured: INSIGHT_CHANGED");
        then(commandMapper).should(never()).map(any(IntegrationEventMessage.class));
        then(learnerInsightAnalyzer).should(never()).analyze(any(LearnerInsightAnalysisCommand.class));
    }

    @Test
    void 같은_이벤트_타입을_지원하는_Command_DTO_Mapper를_중복으로_등록할_수_없다() {
        given(anotherCommandMapper.supportedEventType()).willReturn(COACHING_TURN_DONE);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> new LearnerInsightAnalysisSqsListener(
                        List.of(commandMapper, anotherCommandMapper),
                        learnerInsightAnalyzer
                ));
    }
}
