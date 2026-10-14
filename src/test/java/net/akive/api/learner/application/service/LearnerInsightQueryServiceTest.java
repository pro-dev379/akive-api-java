package net.akive.api.learner.application.service;

import net.akive.api.learner.application.exception.LearnerInsightEmbeddingException;
import net.akive.api.learner.application.required.LearnerInsightRepository;
import net.akive.api.learner.application.required.LearnerInsightVectorStore;
import net.akive.api.learner.application.required.TextEmbeddingModel;
import net.akive.api.learner.application.required.data.TextEmbeddingRequestData;
import net.akive.api.learner.domain.LearnerInsight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static net.akive.api.learner.application.fixture.TextEmbeddingFixture.EMBEDDING_VALUES;
import static net.akive.api.learner.application.fixture.LearnerInsightQueryFixture.*;
import static net.akive.api.learner.application.required.data.TextEmbeddingRequestData.forLearnerInsightSearch;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LearnerInsightQueryServiceTest {

    @Mock
    TextEmbeddingModel textEmbeddingModel;

    @Mock
    LearnerInsightVectorStore learnerInsightVectorStore;

    @Mock
    LearnerInsightRepository learnerInsightRepository;

    @InjectMocks
    LearnerInsightQueryService learnerInsightQueryService;

    @Test
    void 벡터_저장소가_반환한_유사도_순서대로_관련_학습자_인사이트를_조회한다() {
        TextEmbeddingRequestData embeddingRequest = forLearnerInsightSearch(USER_ID, QUERY);
        List<Long> similarLearnerInsightIds = List.of(
                ANOTHER_LEARNER_INSIGHT_ID,
                LEARNER_INSIGHT_ID
        );
        LearnerInsight learnerInsight = reconstructLearnerInsight(LEARNER_INSIGHT_ID);
        LearnerInsight anotherLearnerInsight = reconstructLearnerInsight(ANOTHER_LEARNER_INSIGHT_ID);
        given(textEmbeddingModel.embed(embeddingRequest)).willReturn(EMBEDDING_VALUES);
        given(learnerInsightVectorStore.findSimilarIds(
                USER_ID,
                FILTER_STATUSES,
                EMBEDDING_VALUES,
                SEARCH_RESULT_MAX_COUNT
        )).willReturn(similarLearnerInsightIds);
        given(learnerInsightRepository.findAllByUserIdAndLearnerInsightIdInAndStatusIn(
                USER_ID,
                similarLearnerInsightIds,
                FILTER_STATUSES
        )).willReturn(List.of(learnerInsight, anotherLearnerInsight));

        List<LearnerInsight> relevantInsights = learnerInsightQueryService.findRelevantInsights(
                USER_ID,
                QUERY,
                FILTER_STATUSES,
                SEARCH_RESULT_MAX_COUNT
        );

        assertThat(relevantInsights).containsExactly(anotherLearnerInsight, learnerInsight);
    }

    @Test
    void 벡터_저장소에서_관련_학습자_인사이트를_찾지_못하면_DB에서_정해진_개수만큼_조회한다() {
        TextEmbeddingRequestData embeddingRequest = forLearnerInsightSearch(USER_ID, QUERY);
        LearnerInsight firstInsight = reconstructLearnerInsight(LEARNER_INSIGHT_ID);
        LearnerInsight secondInsight = reconstructLearnerInsight(ANOTHER_LEARNER_INSIGHT_ID);
        LearnerInsight thirdInsight = reconstructLearnerInsight(THIRD_LEARNER_INSIGHT_ID);
        given(textEmbeddingModel.embed(embeddingRequest)).willReturn(EMBEDDING_VALUES);
        given(learnerInsightVectorStore.findSimilarIds(
                USER_ID,
                FILTER_STATUSES,
                EMBEDDING_VALUES,
                SEARCH_RESULT_MAX_COUNT
        )).willReturn(List.of());
        given(learnerInsightRepository.findAllByUserIdAndStatusIn(USER_ID, FILTER_STATUSES))
                .willReturn(List.of(firstInsight, secondInsight, thirdInsight));

        List<LearnerInsight> relevantInsights = learnerInsightQueryService.findRelevantInsights(
                USER_ID,
                QUERY,
                FILTER_STATUSES,
                SEARCH_RESULT_MAX_COUNT
        );

        assertThat(relevantInsights).containsExactly(firstInsight, secondInsight);
    }

    @Test
    void 유사도_비교에_사용할_텍스트를_임베딩하지_못하면_DB에서_관련_학습자_인사이트를_조회한다() {
        TextEmbeddingRequestData embeddingRequest = forLearnerInsightSearch(USER_ID, QUERY);
        LearnerInsight fallbackInsight = reconstructLearnerInsight();
        given(textEmbeddingModel.embed(embeddingRequest))
                .willThrow(new LearnerInsightEmbeddingException("Failed to create an embedding."));
        given(learnerInsightRepository.findAllByUserIdAndStatusIn(USER_ID, FILTER_STATUSES))
                .willReturn(List.of(fallbackInsight));

        List<LearnerInsight> relevantInsights = learnerInsightQueryService.findRelevantInsights(
                USER_ID,
                QUERY,
                FILTER_STATUSES,
                SEARCH_RESULT_MAX_COUNT
        );

        assertThat(relevantInsights).containsExactly(fallbackInsight);
    }

    @Test
    void 벡터_유사도_검색에_실패하면_DB에서_관련_학습자_인사이트를_조회한다() {
        TextEmbeddingRequestData embeddingRequest = forLearnerInsightSearch(USER_ID, QUERY);
        LearnerInsight fallbackInsight = reconstructLearnerInsight();
        given(textEmbeddingModel.embed(embeddingRequest)).willReturn(EMBEDDING_VALUES);
        given(learnerInsightVectorStore.findSimilarIds(
                USER_ID,
                FILTER_STATUSES,
                EMBEDDING_VALUES,
                SEARCH_RESULT_MAX_COUNT
        )).willThrow(new LearnerInsightEmbeddingException("Failed to search vectors."));
        given(learnerInsightRepository.findAllByUserIdAndStatusIn(USER_ID, FILTER_STATUSES))
                .willReturn(List.of(fallbackInsight));

        List<LearnerInsight> relevantInsights = learnerInsightQueryService.findRelevantInsights(
                USER_ID,
                QUERY,
                FILTER_STATUSES,
                SEARCH_RESULT_MAX_COUNT
        );

        assertThat(relevantInsights).containsExactly(fallbackInsight);
    }
}
