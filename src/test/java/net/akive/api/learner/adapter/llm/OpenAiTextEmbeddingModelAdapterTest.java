package net.akive.api.learner.adapter.llm;

import com.openai.client.OpenAIClient;
import com.openai.core.RequestOptions;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.Embedding;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.services.blocking.EmbeddingService;
import net.akive.api.learner.application.exception.LearnerInsightEmbeddingException;
import net.akive.api.learner.application.required.data.TextEmbeddingRequestData;
import net.akive.api.llmusage.application.provided.LlmUsageRecorder;
import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Collections.nCopies;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.*;
import static net.akive.api.core.config.llm.fixture.OpenAiResponseFixture.INPUT_TOKEN_COUNT;
import static net.akive.api.learner.application.fixture.TextEmbeddingFixture.EMBEDDING_VALUES;
import static net.akive.api.learner.application.required.data.TextEmbeddingRequestData.forLearnerInsightSearch;
import static net.akive.api.learner.application.required.data.TextEmbeddingRequestData.forLearnerInsightStorage;
import static net.akive.api.llmusage.application.fixture.LlmUsageRecordCommandFixture.createEmbeddingUsageRecordCommand;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.LEARNER_INSIGHT_EMBEDDING;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.LEARNER_INSIGHT_SEARCH_EMBEDDING;
import static net.akive.api.llmusage.domain.LlmUsageScope.BATCH;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OpenAiTextEmbeddingModelAdapterTest {

    private static final String TEXT = "작은 단위의 계획을 꾸준히 실천한다.";

    @Mock
    OpenAIClient openAIClient;

    @Mock
    EmbeddingService embeddingService;

    @Mock
    LlmUsageRecorder llmUsageRecorder;

    OpenAiTextEmbeddingModelAdapter openAiTextEmbeddingModelAdapter;

    @BeforeEach
    void setUp() {
        openAiTextEmbeddingModelAdapter = new OpenAiTextEmbeddingModelAdapter(
                openAIClient,
                createOpenAiProperties(),
                llmUsageRecorder
        );
    }

    @Test
    void 학습자_인사이트_저장용_텍스트를_OpenAI_임베딩_벡터로_변환하고_LLM_토큰_사용량을_기록한다() {
        TextEmbeddingRequestData request = forLearnerInsightStorage(TEXT);
        prepareEmbeddingResponse(List.of(EMBEDDING_VALUES));

        List<Float> result = openAiTextEmbeddingModelAdapter.embed(request);

        assertThat(result).isEqualTo(EMBEDDING_VALUES);
        assertEmbeddingCreateParams(captureEmbeddingCreateParams());
        then(llmUsageRecorder).should().record(createEmbeddingUsageRecordCommand(
                null,
                LEARNER_INSIGHT_EMBEDDING,
                BATCH
        ));
    }

    @Test
    void 학습자_인사이트_검색용_텍스트를_OpenAI_임베딩_벡터로_생성하고_LLM_토큰_사용량을_기록한다() {
        TextEmbeddingRequestData request = forLearnerInsightSearch(USER_ID, TEXT);
        prepareEmbeddingResponse(List.of(EMBEDDING_VALUES));

        List<Float> result = openAiTextEmbeddingModelAdapter.embed(request);

        assertThat(result).isEqualTo(EMBEDDING_VALUES);
        then(llmUsageRecorder).should().record(createEmbeddingUsageRecordCommand(
                USER_ID,
                LEARNER_INSIGHT_SEARCH_EMBEDDING,
                USER
        ));
    }

    @Test
    void LLM_토큰_사용량_기록에_실패해도_텍스트를_OpenAI_임베딩_벡터를_반환한다() {
        TextEmbeddingRequestData request = forLearnerInsightStorage(TEXT);
        prepareEmbeddingResponse(List.of(EMBEDDING_VALUES));
        willThrow(new IllegalStateException("Usage persistence failed."))
                .given(llmUsageRecorder)
                .record(any(LlmUsageRecordCommand.class));

        List<Float> result = openAiTextEmbeddingModelAdapter.embed(request);

        assertThat(result).isEqualTo(EMBEDDING_VALUES);
    }

    @Test
    void OpenAI_응답에_임베딩_벡터가_정확히_하나_포함되지_않으면_예외를_던진다() {
        TextEmbeddingRequestData request = forLearnerInsightStorage(TEXT);
        prepareEmbeddingResponse(List.of());

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> openAiTextEmbeddingModelAdapter.embed(request))
                .withMessage("OpenAI returned an unexpected number of embeddings: 0");
    }

    @Test
    void OpenAI_응답의_임베딩_벡터_차원이_설정한_차원과_다르면_예외를_던진다() {
        TextEmbeddingRequestData request = forLearnerInsightStorage(TEXT);
        List<Float> unexpectedDimensionEmbedding = nCopies(EMBEDDING_DIMENSIONS - 1, 0.1F);
        prepareEmbeddingResponse(List.of(unexpectedDimensionEmbedding));

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> openAiTextEmbeddingModelAdapter.embed(request))
                .withMessage(
                        "OpenAI returned an embedding with an unexpected dimension: "
                                + unexpectedDimensionEmbedding.size()
                );
    }

    @Test
    void OpenAI_API_호출에_실패하면_학습자_인사이트_임베딩_예외를_던진다() {
        TextEmbeddingRequestData request = forLearnerInsightStorage(TEXT);
        RuntimeException openAiException = new RuntimeException("OpenAI is unavailable.");
        given(openAIClient.embeddings()).willReturn(embeddingService);
        given(embeddingService.create(any(EmbeddingCreateParams.class), any(RequestOptions.class)))
                .willThrow(openAiException);

        assertThatExceptionOfType(LearnerInsightEmbeddingException.class)
                .isThrownBy(() -> openAiTextEmbeddingModelAdapter.embed(request))
                .withMessage("OpenAI failed to create a text embedding.")
                .withCause(openAiException);
    }

    private void prepareEmbeddingResponse(List<List<Float>> embeddingValues) {
        List<Embedding> embeddings = embeddingValues.stream().map(this::createEmbedding).toList();
        CreateEmbeddingResponse.Usage usage = mock(CreateEmbeddingResponse.Usage.class);
        CreateEmbeddingResponse response = mock(CreateEmbeddingResponse.class);

        given(usage.promptTokens()).willReturn(INPUT_TOKEN_COUNT);
        given(response.data()).willReturn(embeddings);
        given(response.usage()).willReturn(usage);
        given(openAIClient.embeddings()).willReturn(embeddingService);
        given(embeddingService.create(any(EmbeddingCreateParams.class), any(RequestOptions.class)))
                .willReturn(response);
    }

    private Embedding createEmbedding(List<Float> embeddingValues) {
        Embedding embedding = mock(Embedding.class);
        given(embedding.embedding()).willReturn(embeddingValues);
        return embedding;
    }

    private EmbeddingCreateParams captureEmbeddingCreateParams() {
        ArgumentCaptor<EmbeddingCreateParams> paramsCaptor = ArgumentCaptor.captor();
        then(embeddingService).should().create(paramsCaptor.capture(), any(RequestOptions.class));
        return paramsCaptor.getValue();
    }

    private void assertEmbeddingCreateParams(EmbeddingCreateParams params) {
        assertThat(params.model().asString()).isEqualTo(EMBEDDING_MODEL);
        assertThat(params.input().asString()).isEqualTo(TEXT);
        assertThat(params.dimensions()).contains((long) EMBEDDING_DIMENSIONS);
    }
}
