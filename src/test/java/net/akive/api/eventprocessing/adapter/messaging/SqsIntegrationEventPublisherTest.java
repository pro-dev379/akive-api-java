package net.akive.api.eventprocessing.adapter.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsSendOptions;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import net.akive.api.core.config.aws.EventSqsProperties;
import net.akive.api.eventprocessing.application.event.IntegrationEventMessage;
import net.akive.api.eventprocessing.application.exception.IntegrationEventPublicationException;
import net.akive.api.eventprocessing.domain.ProducedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.eventprocessing.domain.EventType.INSIGHT_CHANGED;
import static net.akive.api.eventprocessing.domain.fixture.ProducedEventFixture.*;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.LEARNER_INSIGHT_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Answers.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SqsIntegrationEventPublisherTest {

    private static final String LEARNER_INSIGHT_ANALYSIS_QUEUE_NAME =
            "akive-learner_insight_analysis_queue-test.fifo";
    private static final String LEARNER_INSIGHT_EMBEDDING_QUEUE_NAME =
            "akive-learner_insight_embedding_queue-test.fifo";

    private static final String COACHING_TURN_DONE_PAYLOAD = """
            {"userId":%d,"coachingConversationId":10,"userMessageId":100,"assistantMessageId":101}
            """.formatted(USER_ID);
    private static final String INSIGHT_CHANGED_PAYLOAD = """
            {"learnerInsightId":%d,"userId":%d}
            """.formatted(LEARNER_INSIGHT_ID, USER_ID);

    private final EventSqsProperties eventSqsProperties = new EventSqsProperties(
            new EventSqsProperties.LearnerInsightAnalysis(LEARNER_INSIGHT_ANALYSIS_QUEUE_NAME),
            new EventSqsProperties.LearnerInsightEmbedding(LEARNER_INSIGHT_EMBEDDING_QUEUE_NAME)
    );

    @Mock
    SqsTemplate sqsTemplate;

    @Mock(answer = RETURNS_SELF)
    SqsSendOptions<IntegrationEventMessage> sqsSendOptions;

    @Captor
    ArgumentCaptor<Consumer<SqsSendOptions<IntegrationEventMessage>>> sendOptionsCustomizerCaptor;

    SqsIntegrationEventPublisher sqsIntegrationEventPublisher;

    @BeforeEach
    void setUp() {
        sqsIntegrationEventPublisher = new SqsIntegrationEventPublisher(
                sqsTemplate,
                new ObjectMapper(),
                eventSqsProperties
        );
    }

    @Test
    void 지원하는_메시지_큐_이벤트_타입을_반환한다() {
        assertThat(sqsIntegrationEventPublisher.supportedEventTypes())
                .isEqualTo(Set.of(COACHING_TURN_DONE, INSIGHT_CHANGED));
    }

    @Test
    void 코칭_응답_완료_이벤트를_학습자_인사이트_분석_큐에_발행한다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                COACHING_TURN_DONE_PAYLOAD,
                CREATED_AT
        );

        sqsIntegrationEventPublisher.publish(producedEvent);

        captureAndApplySendOptions();
        then(sqsSendOptions).should().queue(LEARNER_INSIGHT_ANALYSIS_QUEUE_NAME);
        then(sqsSendOptions).should().payload(new IntegrationEventMessage(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                COACHING_TURN_DONE_PAYLOAD,
                CREATED_AT
        ));
        then(sqsSendOptions).should().messageGroupId(Long.toString(USER_ID));
        then(sqsSendOptions).should().messageDeduplicationId(FIRST_EVENT_ID.toString());
    }

    @Test
    void 학습자_인사이트_변경_이벤트를_임베딩_큐에_발행한다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                INSIGHT_CHANGED,
                INSIGHT_CHANGED_PAYLOAD,
                CREATED_AT
        );

        sqsIntegrationEventPublisher.publish(producedEvent);

        captureAndApplySendOptions();
        then(sqsSendOptions).should().queue(LEARNER_INSIGHT_EMBEDDING_QUEUE_NAME);
        then(sqsSendOptions).should().payload(new IntegrationEventMessage(
                FIRST_EVENT_ID,
                INSIGHT_CHANGED,
                INSIGHT_CHANGED_PAYLOAD,
                CREATED_AT
        ));
        then(sqsSendOptions).should().messageGroupId(Long.toString(LEARNER_INSIGHT_ID));
        then(sqsSendOptions).should().messageDeduplicationId(FIRST_EVENT_ID.toString());
    }

    @Test
    void 코칭_응답_완료_이벤트에_회원_식별자가_없으면_메시지_큐에_이벤트를_발행할_수_없다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                "{\"coachingConversationId\":10}",
                CREATED_AT
        );

        assertThat(assertPublicationFailure(producedEvent, FIRST_EVENT_ID))
                .hasCauseInstanceOf(IntegrationEventPublicationException.class);
        then(sqsTemplate).should(never()).send(sendOptionsCustomizerCaptor.capture());
    }

    @Test
    void 학습자_인사이트_변경_이벤트에_학습자_인사이트_식별자가_없으면_메시지_큐에_이벤트를_발행할_수_없다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                SECOND_EVENT_ID,
                INSIGHT_CHANGED,
                "{\"userId\":%d}".formatted(USER_ID),
                CREATED_AT
        );

        assertThat(assertPublicationFailure(producedEvent, SECOND_EVENT_ID))
                .hasCauseInstanceOf(IntegrationEventPublicationException.class);
        then(sqsTemplate).should(never()).send(sendOptionsCustomizerCaptor.capture());
    }

    @Test
    void 이벤트_페이로드가_JSON_형식이_아니면_메시지_큐에_이벤트를_발행할_수_없다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                "invalid-payload",
                CREATED_AT
        );

        assertThat(assertPublicationFailure(producedEvent, FIRST_EVENT_ID))
                .hasCauseInstanceOf(JsonProcessingException.class);
        then(sqsTemplate).should(never()).send(sendOptionsCustomizerCaptor.capture());
    }

    @Test
    void SQS_전송에_실패하면_이벤트_발행_예외가_발생한다() {
        ProducedEvent producedEvent = ProducedEvent.create(
                FIRST_EVENT_ID,
                COACHING_TURN_DONE,
                COACHING_TURN_DONE_PAYLOAD,
                CREATED_AT
        );
        RuntimeException sqsException = new RuntimeException("SQS is unavailable.");
        willThrow(sqsException).given(sqsTemplate).send(any());

        assertThat(assertPublicationFailure(producedEvent, FIRST_EVENT_ID))
                .hasCause(sqsException);
    }

    private void captureAndApplySendOptions() {
        then(sqsTemplate).should().send(sendOptionsCustomizerCaptor.capture());
        sendOptionsCustomizerCaptor.getValue().accept(sqsSendOptions);
    }

    private IntegrationEventPublicationException assertPublicationFailure(
            ProducedEvent producedEvent,
            UUID eventId
    ) {
        IntegrationEventPublicationException exception = catchThrowableOfType(
                IntegrationEventPublicationException.class,
                () -> sqsIntegrationEventPublisher.publish(producedEvent)
        );

        assertThat(exception).hasMessage("Failed to publish integration event to SQS: " + eventId);

        return exception;
    }
}
