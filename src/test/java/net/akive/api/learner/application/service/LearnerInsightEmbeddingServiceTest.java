package net.akive.api.learner.application.service;

import net.akive.api.eventprocessing.application.provided.EventProcessingTracker;
import net.akive.api.learner.application.event.LearnerInsightChangedEvent;
import net.akive.api.learner.application.exception.LearnerInsightEmbeddingException;
import net.akive.api.learner.application.required.LearnerInsightVectorStore;
import net.akive.api.learner.application.required.TextEmbeddingModel;
import net.akive.api.learner.application.required.data.LearnerInsightVectorData;
import net.akive.api.learner.application.required.data.TextEmbeddingRequestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.akive.api.eventprocessing.domain.EventConsumer.INSIGHT_EMBEDDING;
import static net.akive.api.learner.application.fixture.LearnerInsightEventFixture.createLearnerInsightChangedEvent;
import static net.akive.api.learner.application.fixture.TextEmbeddingFixture.EMBEDDING_VALUES;
import static net.akive.api.learner.application.required.data.TextEmbeddingRequestData.forLearnerInsightStorage;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LearnerInsightEmbeddingServiceTest {

    @Mock
    EventProcessingTracker eventProcessingTracker;

    @Mock
    TextEmbeddingModel textEmbeddingModel;

    @Mock
    LearnerInsightVectorStore learnerInsightVectorStore;

    @InjectMocks
    LearnerInsightEmbeddingService learnerInsightEmbeddingService;

    @Test
    void 학습자_인사이트_내용을_임베딩하고_벡터_저장소에_저장한_후_이벤트_처리_완료_처리한다() {
        LearnerInsightChangedEvent event = createLearnerInsightChangedEvent();
        TextEmbeddingRequestData embeddingRequest = forLearnerInsightStorage(event.content());
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                event.learnerInsightId(),
                event.userId(),
                event.status()
        );
        given(eventProcessingTracker.isProcessed(event.eventId(), INSIGHT_EMBEDDING)).willReturn(false);
        given(textEmbeddingModel.embed(embeddingRequest)).willReturn(EMBEDDING_VALUES);

        learnerInsightEmbeddingService.update(event);

        then(learnerInsightVectorStore).should().save(learnerInsight, EMBEDDING_VALUES);
        then(eventProcessingTracker).should().markProcessed(
                event.eventId(),
                event.eventType(),
                INSIGHT_EMBEDDING
        );
    }

    @Test
    void 이미_처리한_이벤트로_학습자_인사이트의_임베딩_벡터를_중복_저장하지_않는다() {
        LearnerInsightChangedEvent event = createLearnerInsightChangedEvent();
        given(eventProcessingTracker.isProcessed(event.eventId(), INSIGHT_EMBEDDING)).willReturn(true);

        learnerInsightEmbeddingService.update(event);

        then(textEmbeddingModel).shouldHaveNoInteractions();
        then(learnerInsightVectorStore).shouldHaveNoInteractions();
        then(eventProcessingTracker).should(never()).markProcessed(any(), any(), any());
    }

    @Test
    void 학습자_인사이트_내용을_임베딩하지_못하면_예외를_던진다() {
        LearnerInsightChangedEvent event = createLearnerInsightChangedEvent();
        TextEmbeddingRequestData embeddingRequest = forLearnerInsightStorage(event.content());
        LearnerInsightEmbeddingException embeddingException =
                new LearnerInsightEmbeddingException("Failed to create an embedding.");
        given(eventProcessingTracker.isProcessed(event.eventId(), INSIGHT_EMBEDDING)).willReturn(false);
        given(textEmbeddingModel.embed(embeddingRequest)).willThrow(embeddingException);

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> learnerInsightEmbeddingService.update(event))
                .isSameAs(embeddingException);

        then(learnerInsightVectorStore).shouldHaveNoInteractions();
        then(eventProcessingTracker).should(never()).markProcessed(any(), any(), any());
    }

    @Test
    void 학습자_인사이트의_임베딩_벡터를_저장하지_못하면_예외를_던진다() {
        LearnerInsightChangedEvent event = createLearnerInsightChangedEvent();
        TextEmbeddingRequestData embeddingRequest = forLearnerInsightStorage(event.content());
        LearnerInsightVectorData learnerInsight = new LearnerInsightVectorData(
                event.learnerInsightId(),
                event.userId(),
                event.status()
        );
        LearnerInsightEmbeddingException embeddingException =
                new LearnerInsightEmbeddingException("Failed to save an embedding.");
        given(eventProcessingTracker.isProcessed(event.eventId(), INSIGHT_EMBEDDING)).willReturn(false);
        given(textEmbeddingModel.embed(embeddingRequest)).willReturn(EMBEDDING_VALUES);
        willThrow(embeddingException)
                .given(learnerInsightVectorStore)
                .save(learnerInsight, EMBEDDING_VALUES);

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> learnerInsightEmbeddingService.update(event))
                .isSameAs(embeddingException);

        then(eventProcessingTracker).should(never()).markProcessed(any(), any(), any());
    }
}
