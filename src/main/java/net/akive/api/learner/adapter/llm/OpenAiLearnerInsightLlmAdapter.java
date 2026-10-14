package net.akive.api.learner.adapter.llm;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.openai.client.OpenAIClient;
import com.openai.core.RequestOptions;
import com.openai.models.responses.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.akive.api.core.config.llm.OpenAiProperties;
import net.akive.api.learner.application.exception.LearnerInsightAnalysisException;
import net.akive.api.learner.application.prompt.LearnerInsightPrompt;
import net.akive.api.learner.application.prompt.LearnerInsightPromptFactory;
import net.akive.api.learner.application.required.LearnerInsightLlm;
import net.akive.api.learner.application.required.data.InsightAction;
import net.akive.api.learner.application.required.data.InsightSuggestionData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisRequestData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;
import net.akive.api.learner.domain.EvidenceStance;
import net.akive.api.learner.domain.InsightType;
import net.akive.api.llmusage.application.provided.LlmUsageRecorder;
import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static net.akive.api.learner.domain.LearnerPolicy.INSIGHT_ANALYSIS_OUTPUT_TOKEN_LIMIT;
import static net.akive.api.llmusage.domain.LlmProvider.OPENAI;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.LEARNER_INSIGHT_ANALYSIS;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.*;
import static net.akive.api.llmusage.domain.LlmUsageScope.BATCH;

@Component
@Slf4j
@RequiredArgsConstructor
class OpenAiLearnerInsightLlmAdapter implements LearnerInsightLlm {

    private final OpenAIClient openAIClient;
    private final OpenAiProperties openAiProperties;
    private final LearnerInsightPromptFactory learnerInsightPromptFactory;
    private final LlmUsageRecorder llmUsageRecorder;

    @Override
    public LearnerInsightAnalysisResultData analyze(LearnerInsightAnalysisRequestData request) {
        LearnerInsightPrompt insightPrompt = learnerInsightPromptFactory.createAnalysis(request);

        try {
            StructuredResponse<LearnerInsightAnalysisOutput> response = openAIClient.responses().create(
                    createAnalysisParams(insightPrompt),
                    RequestOptions.builder().timeout(openAiProperties.timeout()).build()
            );

            recordUsageSafely(response);

            return extractAnalysisResult(response);
        } catch (LearnerInsightAnalysisException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new LearnerInsightAnalysisException("OpenAI failed to analyze learner insights.", e);
        }
    }

    private void recordUsageSafely(StructuredResponse<LearnerInsightAnalysisOutput> response) {
        response.usage().ifPresent(usage -> {
            try {
                llmUsageRecorder.record(new LlmUsageRecordCommand(
                        null,
                        LEARNER_INSIGHT_ANALYSIS,
                        OPENAI,
                        response.id(),
                        openAiProperties.model().learnerInsight(),
                        BATCH,
                        response.status()
                                .map(status -> {
                                    if (status.equals(ResponseStatus.COMPLETED)) {
                                        return COMPLETED;
                                    }
                                    if (status.equals(ResponseStatus.INCOMPLETE)) {
                                        return INCOMPLETE;
                                    }
                                    return FAILED;
                                })
                                .orElse(FAILED),
                        usage.inputTokens(),
                        usage.inputTokensDetails().cachedTokens(),
                        usage.outputTokens(),
                        usage.outputTokensDetails().reasoningTokens()
                ));
            } catch (RuntimeException e) {
                log.warn(
                        "Failed to record OpenAI learner insight analysis usage: providerResponseId={}",
                        response.id(),
                        e
                );
            }
        });
    }

    private StructuredResponseCreateParams<LearnerInsightAnalysisOutput> createAnalysisParams(
            LearnerInsightPrompt insightPrompt
    ) {
        return ResponseCreateParams.builder()
                .model(openAiProperties.model().learnerInsight())
                .instructions(insightPrompt.instructions())
                .input(insightPrompt.input())
                .maxOutputTokens(INSIGHT_ANALYSIS_OUTPUT_TOKEN_LIMIT)
                .store(false)
                .text(LearnerInsightAnalysisOutput.class)
                .build();
    }

    private LearnerInsightAnalysisResultData extractAnalysisResult(
            StructuredResponse<LearnerInsightAnalysisOutput> response
    ) {
        if (response.status().filter(ResponseStatus.COMPLETED::equals).isEmpty()) {
            throw new LearnerInsightAnalysisException(
                    "OpenAI learner insight analysis did not complete: status="
                            + response.status().map(ResponseStatus::asString).orElse("missing")
            );
        }

        Optional<String> refusal = response.output().stream()
                .flatMap(output -> output.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.refusal().stream())
                .map(ResponseOutputRefusal::refusal)
                .findFirst();

        if (refusal.isPresent()) {
            throw new LearnerInsightAnalysisException("OpenAI refused learner insight analysis: " + refusal.get());
        }

        LearnerInsightAnalysisOutput output = response.output().stream()
                .flatMap(responseOutput -> responseOutput.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .findFirst()
                .orElseThrow(() -> new LearnerInsightAnalysisException(
                        "OpenAI returned no learner insight analysis output."
                ));

        if (output.getSuggestions() == null) {
            throw new LearnerInsightAnalysisException(
                    "OpenAI returned learner insight analysis without suggestions."
            );
        }

        return new LearnerInsightAnalysisResultData(output.getSuggestions().stream()
                .map(this::toSuggestionData)
                .toList());
    }

    private InsightSuggestionData toSuggestionData(LearnerInsightSuggestionOutput suggestion) {
        if (suggestion == null) {
            throw new LearnerInsightAnalysisException("OpenAI returned a null learner insight suggestion.");
        }

        return new InsightSuggestionData(
                suggestion.getAction(),
                valueOf(suggestion.getTargetInsightId()),
                toInsightType(suggestion.getInsightType()),
                valueOf(suggestion.getContent()),
                toEvidenceStance(suggestion.getStance()),
                valueOf(suggestion.getEvidenceContent())
        );
    }

    private static InsightType toInsightType(LearnerInsightTypeOutput insightType) {
        if (insightType == null || insightType == LearnerInsightTypeOutput.NOT_APPLICABLE) {
            return null;
        }

        return InsightType.valueOf(insightType.name());
    }

    private static EvidenceStance toEvidenceStance(EvidenceStanceOutput stance) {
        if (stance == null || stance == EvidenceStanceOutput.NOT_APPLICABLE) {
            return null;
        }

        return EvidenceStance.valueOf(stance.name());
    }

    private static <T> T valueOf(Optional<T> value) {
        return value.orElse(null);
    }

    @Getter
    @JsonClassDescription("Learner Insight 분석 결과")
    static final class LearnerInsightAnalysisOutput {

        @JsonPropertyDescription("Evidence와 현재 Learner Insight를 비교하여 도출한 변경 제안 목록")
        private List<LearnerInsightSuggestionOutput> suggestions;
    }

    @Getter
    @JsonClassDescription("Learner Insight에 적용할 변경 제안")
    static final class LearnerInsightSuggestionOutput {

        @JsonPropertyDescription("적용할 변경 유형")
        private InsightAction action;

        @JsonPropertyDescription("REINFORCE, CONTRADICT, SUPERSEDE 대상인 기존 Learner Insight ID이며, 그 외에는 null")
        private Optional<Long> targetInsightId;

        @JsonPropertyDescription("CREATE 또는 SUPERSEDE로 생성할 새 Insight 유형이며, 그 외에는 NOT_APPLICABLE")
        private LearnerInsightTypeOutput insightType;

        @JsonPropertyDescription("CREATE 또는 SUPERSEDE로 생성할 새 Insight 내용이며, 그 외에는 null")
        private Optional<String> content;

        @JsonPropertyDescription("변경 유형에 해당하는 Evidence 입장이며, NONE이면 NOT_APPLICABLE")
        private EvidenceStanceOutput stance;

        @JsonPropertyDescription("Evidence 자료에만 근거한 간결한 근거 요약이며, NONE이면 null")
        private Optional<String> evidenceContent;
    }

    /**
     * OpenAI 구조화 출력에서 사용하는 Insight의 Type이다.
     *
     * @implNote openai-java 4.80.0은 {@code Optional<enum>}에서 생성한 nullable enum 스키마의 로컬 검증에 실패한다.
     * SDK 업데이트 시 {@code Optional<InsightType>}으로 대체 가능한지 다시 확인해야 한다.
     */
    enum LearnerInsightTypeOutput {
        PREFERENCE,
        STRENGTH,
        WEAKNESS,
        BEHAVIOR_PATTERN,
        RECURRING_PATTERN,
        EFFECTIVE_STRATEGY,
        NOT_APPLICABLE
    }

    /**
     * OpenAI 구조화 출력에서 사용하는 Evidence의 Stance다.
     *
     * @implNote openai-java 4.80.0은 {@code Optional<enum>}에서 생성한 nullable enum 스키마의 로컬 검증에 실패한다.
     * SDK 업데이트 시 {@code Optional<EvidenceStance>}으로 대체 가능한지 다시 확인해야 한다.
     */
    enum EvidenceStanceOutput {
        SUPPORTING,
        CONTRADICTING,
        NOT_APPLICABLE
    }
}
