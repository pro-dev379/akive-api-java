package net.akive.api.learner.adapter.llm;

import com.openai.client.OpenAIClient;
import com.openai.core.RequestOptions;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.core.config.llm.OpenAiProperties;
import net.akive.api.learner.application.exception.LearnerInsightEmbeddingException;
import net.akive.api.learner.application.required.TextEmbeddingModel;
import net.akive.api.learner.application.required.data.TextEmbeddingRequestData;
import net.akive.api.llmusage.application.provided.LlmUsageRecorder;
import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import net.akive.api.llmusage.domain.LlmUsagePurpose;
import net.akive.api.llmusage.domain.LlmUsageScope;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.akive.api.llmusage.domain.LlmProvider.OPENAI;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.LEARNER_INSIGHT_EMBEDDING;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.LEARNER_INSIGHT_SEARCH_EMBEDDING;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageScope.BATCH;
import static net.akive.api.llmusage.domain.LlmUsageScope.USER;

@Component
@Slf4j
@RequiredArgsConstructor
class OpenAiTextEmbeddingModelAdapter implements TextEmbeddingModel {

    private final OpenAIClient openAIClient;
    private final OpenAiProperties openAiProperties;
    private final LlmUsageRecorder llmUsageRecorder;

    @Override
    public List<Float> embed(TextEmbeddingRequestData request) {
        try {
            CreateEmbeddingResponse response = openAIClient.embeddings().create(
                    EmbeddingCreateParams.builder()
                            .model(openAiProperties.model().embed())
                            .input(request.text())
                            .dimensions(openAiProperties.embedding().dimensions())
                            .build(),
                    RequestOptions.builder().timeout(openAiProperties.timeout()).build()
            );

            recordUsageSafely(request, response);

            if (response.data().size() != 1) {
                throw new LearnerInsightEmbeddingException(
                        "OpenAI returned an unexpected number of embeddings: " + response.data().size()
                );
            }

            List<Float> embedding = List.copyOf(response.data().getFirst().embedding());

            if (embedding.size() != openAiProperties.embedding().dimensions()) {
                throw new LearnerInsightEmbeddingException(
                        "OpenAI returned an embedding with an unexpected dimension: " + embedding.size()
                );
            }

            return embedding;
        } catch (LearnerInsightEmbeddingException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new LearnerInsightEmbeddingException("OpenAI failed to create a text embedding.", e);
        }
    }

    private void recordUsageSafely(TextEmbeddingRequestData request, CreateEmbeddingResponse response) {
        LlmUsagePurpose purpose = switch (request.purpose()) {
            case LEARNER_INSIGHT_STORAGE -> LEARNER_INSIGHT_EMBEDDING;
            case LEARNER_INSIGHT_SEARCH -> LEARNER_INSIGHT_SEARCH_EMBEDDING;
        };
        LlmUsageScope usageScope = switch (request.purpose()) {
            case LEARNER_INSIGHT_STORAGE -> BATCH;
            case LEARNER_INSIGHT_SEARCH -> USER;
        };

        try {
            llmUsageRecorder.record(new LlmUsageRecordCommand(
                    request.userId(),
                    purpose,
                    OPENAI,
                    null,
                    openAiProperties.model().embed(),
                    usageScope,
                    COMPLETED,
                    response.usage().promptTokens(),
                    0,
                    0,
                    0
            ));
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to record OpenAI embedding usage: userId={}, purpose={}",
                    request.userId(),
                    purpose,
                    e
            );
        }
    }
}
