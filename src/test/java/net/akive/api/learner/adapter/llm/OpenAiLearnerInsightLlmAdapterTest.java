package net.akive.api.learner.adapter.llm;

import com.openai.client.OpenAIClient;
import com.openai.core.RequestOptions;
import com.openai.models.responses.*;
import com.openai.services.blocking.ResponseService;
import net.akive.api.learner.adapter.llm.OpenAiLearnerInsightLlmAdapter.EvidenceStanceOutput;
import net.akive.api.learner.adapter.llm.OpenAiLearnerInsightLlmAdapter.LearnerInsightAnalysisOutput;
import net.akive.api.learner.adapter.llm.OpenAiLearnerInsightLlmAdapter.LearnerInsightSuggestionOutput;
import net.akive.api.learner.adapter.llm.OpenAiLearnerInsightLlmAdapter.LearnerInsightTypeOutput;
import net.akive.api.learner.application.exception.LearnerInsightAnalysisException;
import net.akive.api.learner.application.prompt.LearnerInsightPrompt;
import net.akive.api.learner.application.prompt.LearnerInsightPromptFactory;
import net.akive.api.learner.application.required.data.InsightAction;
import net.akive.api.learner.application.required.data.InsightSuggestionData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisRequestData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;
import net.akive.api.learner.domain.EvidenceStance;
import net.akive.api.learner.domain.InsightType;
import net.akive.api.llmusage.application.provided.LlmUsageRecorder;
import net.akive.api.llmusage.application.provided.dto.LlmUsageRecordCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.LEARNER_INSIGHT_MODEL;
import static net.akive.api.core.config.llm.fixture.OpenAiPropertiesFixture.createOpenAiProperties;
import static net.akive.api.core.config.llm.fixture.OpenAiResponseFixture.createResponseUsage;
import static net.akive.api.learner.adapter.llm.OpenAiLearnerInsightLlmAdapter.EvidenceStanceOutput.NOT_APPLICABLE;
import static net.akive.api.learner.adapter.llm.OpenAiLearnerInsightLlmAdapter.EvidenceStanceOutput.SUPPORTING;
import static net.akive.api.learner.adapter.llm.OpenAiLearnerInsightLlmAdapter.LearnerInsightTypeOutput.BEHAVIOR_PATTERN;
import static net.akive.api.learner.application.fixture.LearnerInsightAnalysisFixture.ANALYSIS_EVIDENCE_CONTENT;
import static net.akive.api.learner.application.fixture.LearnerInsightAnalysisFixture.createLearnerInsightAnalysisRequestData;
import static net.akive.api.learner.application.required.data.InsightAction.CREATE;
import static net.akive.api.learner.application.required.data.InsightAction.REINFORCE;
import static net.akive.api.learner.domain.LearnerPolicy.INSIGHT_ANALYSIS_OUTPUT_TOKEN_LIMIT;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.LEARNER_INSIGHT_ID;
import static net.akive.api.llmusage.application.fixture.LlmUsageRecordCommandFixture.createLlmUsageRecordCommand;
import static net.akive.api.llmusage.domain.LlmUsagePurpose.LEARNER_INSIGHT_ANALYSIS;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.COMPLETED;
import static net.akive.api.llmusage.domain.LlmUsageResultStatus.INCOMPLETE;
import static net.akive.api.llmusage.domain.LlmUsageScope.BATCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OpenAiLearnerInsightLlmAdapterTest {

    private static final String RESPONSE_ID = "resp_learner_insight_123";
    private static final String INSTRUCTIONS = "학습자의 특성과 반복 패턴을 분석하세요.";
    private static final String NEW_INSIGHT_CONTENT = "작은 단위의 계획을 꾸준히 실천한다.";
    private static final String EVIDENCE_CONTENT = "작은 계획을 꾸준히 실천한다고 말했다.";

    @Mock
    OpenAIClient openAIClient;

    @Mock
    ResponseService responseService;

    @Mock
    LearnerInsightPromptFactory learnerInsightPromptFactory;

    @Mock
    LlmUsageRecorder llmUsageRecorder;

    @Mock
    StructuredResponse<LearnerInsightAnalysisOutput> response;

    OpenAiLearnerInsightLlmAdapter openAiLearnerInsightLlmAdapter;

    @BeforeEach
    void setUp() {
        openAiLearnerInsightLlmAdapter = new OpenAiLearnerInsightLlmAdapter(
                openAIClient,
                createOpenAiProperties(),
                learnerInsightPromptFactory,
                llmUsageRecorder
        );
    }

    @Test
    void OpenAI_응답을_학습자_인사이트_분석_결과로_변환하고_LLM_토큰_사용량을_기록한다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        var createSuggestion = createSuggestionOutput(
                CREATE,
                null,
                BEHAVIOR_PATTERN,
                NEW_INSIGHT_CONTENT,
                SUPPORTING,
                EVIDENCE_CONTENT
        );
        var reinforceSuggestion = createSuggestionOutput(
                REINFORCE,
                LEARNER_INSIGHT_ID,
                LearnerInsightTypeOutput.NOT_APPLICABLE,
                null,
                NOT_APPLICABLE,
                EVIDENCE_CONTENT
        );
        prepareResponse(request, prompt, createAnalysisOutput(List.of(createSuggestion, reinforceSuggestion)));
        given(response.id()).willReturn(RESPONSE_ID);
        given(response.usage()).willReturn(Optional.of(createResponseUsage()));

        LearnerInsightAnalysisResultData result = openAiLearnerInsightLlmAdapter.analyze(request);

        assertThat(result.suggestions()).containsExactly(
                new InsightSuggestionData(
                        CREATE,
                        null,
                        InsightType.BEHAVIOR_PATTERN,
                        NEW_INSIGHT_CONTENT,
                        EvidenceStance.SUPPORTING,
                        EVIDENCE_CONTENT
                ),
                new InsightSuggestionData(
                        REINFORCE,
                        LEARNER_INSIGHT_ID,
                        null,
                        null,
                        null,
                        EVIDENCE_CONTENT
                )
        );
        assertAnalysisParams(captureAnalysisParams(), prompt);
        then(llmUsageRecorder).should().record(createLlmUsageRecordCommand(
                null,
                LEARNER_INSIGHT_ANALYSIS,
                RESPONSE_ID,
                LEARNER_INSIGHT_MODEL,
                BATCH,
                COMPLETED
        ));
    }

    @Test
    void OpenAI_사용량_기록에_실패해도_학습자_인사이트_분석_결과를_반환한다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        prepareResponse(request, prompt, createAnalysisOutput(List.of()));
        given(response.id()).willReturn(RESPONSE_ID);
        given(response.usage()).willReturn(Optional.of(createResponseUsage()));
        willThrow(new IllegalStateException("Usage persistence failed."))
                .given(llmUsageRecorder)
                .record(any(LlmUsageRecordCommand.class));

        LearnerInsightAnalysisResultData result = openAiLearnerInsightLlmAdapter.analyze(request);

        assertThat(result.suggestions()).isEmpty();
    }

    @Test
    void OpenAI_응답이_미완료_상태이면_학습자_인사이트_분석_예외를_던진다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        prepareOpenAiCall(request, prompt);
        given(response.status()).willReturn(Optional.of(ResponseStatus.INCOMPLETE));
        given(response.id()).willReturn(RESPONSE_ID);
        given(response.usage()).willReturn(Optional.of(createResponseUsage()));

        assertThatExceptionOfType(LearnerInsightAnalysisException.class)
                .isThrownBy(() -> openAiLearnerInsightLlmAdapter.analyze(request))
                .withMessage("OpenAI learner insight analysis did not complete: status=incomplete");
        then(llmUsageRecorder).should().record(createLlmUsageRecordCommand(
                null,
                LEARNER_INSIGHT_ANALYSIS,
                RESPONSE_ID,
                LEARNER_INSIGHT_MODEL,
                BATCH,
                INCOMPLETE
        ));
    }

    @Test
    void OpenAI가_학습자_인사이트_분석을_거절하면_예외를_던진다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        ResponseOutputRefusal refusal = mock(ResponseOutputRefusal.class);
        given(refusal.refusal()).willReturn("요청을 처리할 수 없습니다.");
        prepareRefusalResponse(request, prompt, refusal);

        assertThatExceptionOfType(LearnerInsightAnalysisException.class)
                .isThrownBy(() -> openAiLearnerInsightLlmAdapter.analyze(request))
                .withMessage("OpenAI refused learner insight analysis: 요청을 처리할 수 없습니다.");
    }

    @Test
    void OpenAI의_학습자_인사이트_분석_결과가_없으면_예외를_던진다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        prepareResponse(request, prompt, null);

        assertThatExceptionOfType(LearnerInsightAnalysisException.class)
                .isThrownBy(() -> openAiLearnerInsightLlmAdapter.analyze(request))
                .withMessage("OpenAI returned no learner insight analysis output.");
    }

    @Test
    void OpenAI의_학습자_인사이트_분석_결과에_학습자_인사이트_변경_제안_목록이_없으면_예외를_던진다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        prepareResponse(request, prompt, createAnalysisOutput(null));

        assertThatExceptionOfType(LearnerInsightAnalysisException.class)
                .isThrownBy(() -> openAiLearnerInsightLlmAdapter.analyze(request))
                .withMessage("OpenAI returned learner insight analysis without suggestions.");
    }

    @Test
    void OpenAI의_학습자_인사이트_분석_결과에서_학습자_인사이트_변경_제안_목록에_null_항목이_있으면_예외를_던진다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        prepareResponse(request, prompt, createAnalysisOutput(singletonList(null)));

        assertThatExceptionOfType(LearnerInsightAnalysisException.class)
                .isThrownBy(() -> openAiLearnerInsightLlmAdapter.analyze(request))
                .withMessage("OpenAI returned a null learner insight suggestion.");
    }

    @Test
    void OpenAI_API_호출에_실패하면_예외를_던진다() {
        LearnerInsightAnalysisRequestData request = createLearnerInsightAnalysisRequestData();
        LearnerInsightPrompt prompt = new LearnerInsightPrompt(INSTRUCTIONS, ANALYSIS_EVIDENCE_CONTENT);
        RuntimeException openAiException = new RuntimeException("OpenAI is unavailable.");
        given(learnerInsightPromptFactory.createAnalysis(request)).willReturn(prompt);
        given(openAIClient.responses()).willReturn(responseService);
        given(responseService.create(
                ArgumentMatchers.<StructuredResponseCreateParams<LearnerInsightAnalysisOutput>>any(),
                any(RequestOptions.class)
        )).willThrow(openAiException);

        assertThatExceptionOfType(LearnerInsightAnalysisException.class)
                .isThrownBy(() -> openAiLearnerInsightLlmAdapter.analyze(request))
                .withMessage("OpenAI failed to analyze learner insights.")
                .withCause(openAiException);
    }

    private void prepareResponse(
            LearnerInsightAnalysisRequestData request,
            LearnerInsightPrompt prompt,
            LearnerInsightAnalysisOutput output
    ) {
        prepareOpenAiCall(request, prompt);
        List<StructuredResponseOutputItem<LearnerInsightAnalysisOutput>> outputItems =
                output == null
                        ? List.of()
                        : List.of(createOutputItem(output));
        given(response.status()).willReturn(Optional.of(ResponseStatus.COMPLETED));
        given(response.output()).willReturn(outputItems);
    }

    private void prepareOpenAiCall(
            LearnerInsightAnalysisRequestData request,
            LearnerInsightPrompt prompt
    ) {
        given(learnerInsightPromptFactory.createAnalysis(request)).willReturn(prompt);
        given(openAIClient.responses()).willReturn(responseService);
        given(responseService.create(
                ArgumentMatchers.<StructuredResponseCreateParams<LearnerInsightAnalysisOutput>>any(),
                any(RequestOptions.class)
        )).willReturn(response);
    }

    private void prepareRefusalResponse(
            LearnerInsightAnalysisRequestData request,
            LearnerInsightPrompt prompt,
            ResponseOutputRefusal refusal
    ) {
        prepareOpenAiCall(request, prompt);
        StructuredResponseOutputItem<LearnerInsightAnalysisOutput> refusalOutputItem =
                createRefusalOutputItem(refusal);
        given(response.status()).willReturn(Optional.of(ResponseStatus.COMPLETED));
        given(response.output()).willReturn(List.of(refusalOutputItem));
    }

    private LearnerInsightAnalysisOutput createAnalysisOutput(
            List<LearnerInsightSuggestionOutput> suggestions
    ) {
        LearnerInsightAnalysisOutput output = mock(LearnerInsightAnalysisOutput.class);
        given(output.getSuggestions()).willReturn(suggestions);

        return output;
    }

    private LearnerInsightSuggestionOutput createSuggestionOutput(
            InsightAction action,
            Long targetInsightId,
            LearnerInsightTypeOutput insightType,
            String content,
            EvidenceStanceOutput stance,
            String evidenceContent
    ) {
        LearnerInsightSuggestionOutput suggestion = mock(LearnerInsightSuggestionOutput.class);
        given(suggestion.getAction()).willReturn(action);
        given(suggestion.getTargetInsightId()).willReturn(Optional.ofNullable(targetInsightId));
        given(suggestion.getInsightType()).willReturn(insightType);
        given(suggestion.getContent()).willReturn(Optional.ofNullable(content));
        given(suggestion.getStance()).willReturn(stance);
        given(suggestion.getEvidenceContent()).willReturn(Optional.ofNullable(evidenceContent));

        return suggestion;
    }

    private StructuredResponseOutputItem<LearnerInsightAnalysisOutput> createOutputItem(
            LearnerInsightAnalysisOutput output
    ) {
        StructuredResponseOutputMessage.Content<LearnerInsightAnalysisOutput> content = mock();
        given(content.outputText()).willReturn(Optional.of(output));
        StructuredResponseOutputMessage<LearnerInsightAnalysisOutput> message = mock();
        given(message.content()).willReturn(List.of(content));
        StructuredResponseOutputItem<LearnerInsightAnalysisOutput> outputItem = mock();
        given(outputItem.message()).willReturn(Optional.of(message));

        return outputItem;
    }

    private StructuredResponseOutputItem<LearnerInsightAnalysisOutput> createRefusalOutputItem(
            ResponseOutputRefusal refusal
    ) {
        StructuredResponseOutputMessage.Content<LearnerInsightAnalysisOutput> content = mock();
        given(content.refusal()).willReturn(Optional.of(refusal));
        StructuredResponseOutputMessage<LearnerInsightAnalysisOutput> message = mock();
        given(message.content()).willReturn(List.of(content));
        StructuredResponseOutputItem<LearnerInsightAnalysisOutput> outputItem = mock();
        given(outputItem.message()).willReturn(Optional.of(message));

        return outputItem;
    }

    private StructuredResponseCreateParams<LearnerInsightAnalysisOutput> captureAnalysisParams() {
        ArgumentCaptor<StructuredResponseCreateParams<LearnerInsightAnalysisOutput>> paramsCaptor =
                ArgumentCaptor.captor();
        then(responseService).should().create(paramsCaptor.capture(), any(RequestOptions.class));

        return paramsCaptor.getValue();
    }

    private void assertAnalysisParams(
            StructuredResponseCreateParams<LearnerInsightAnalysisOutput> params,
            LearnerInsightPrompt prompt
    ) {
        ResponseCreateParams rawParams = params.rawParams();

        assertThat(params.responseType()).isEqualTo(LearnerInsightAnalysisOutput.class);
        assertThat(rawParams.model().orElseThrow().asString()).isEqualTo(LEARNER_INSIGHT_MODEL);
        assertThat(rawParams.instructions()).contains(prompt.instructions());
        assertThat(rawParams.input().orElseThrow().asText()).isEqualTo(prompt.input());
        assertThat(rawParams.maxOutputTokens()).contains(INSIGHT_ANALYSIS_OUTPUT_TOKEN_LIMIT);
        assertThat(rawParams.store()).contains(false);
    }
}
