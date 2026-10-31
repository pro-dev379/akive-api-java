package net.akive.api.learner.application.service;

import net.akive.api.eventprocessing.application.provided.EventProcessingTracker;
import net.akive.api.learner.application.provided.LearnerInsightFinder;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisMaterial;
import net.akive.api.learner.application.required.LearnerInsightLlm;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisRequestData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;
import net.akive.api.learner.domain.LearnerInsight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CONTENT;
import static net.akive.api.eventprocessing.domain.EventConsumer.LEARNER_INSIGHT;
import static net.akive.api.learner.application.fixture.LearnerInsightAnalysisFixture.createLearnerInsightAnalysisCommand;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.EVIDENCE;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.REFERENCE;
import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;
import static net.akive.api.learner.domain.LearnerPolicy.ANALYSIS_TARGET_MAX_COUNT;
import static net.akive.api.learner.domain.fixture.LearnerInsightEvidenceFixture.ANOTHER_EVIDENCE_CONTENT;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.reconstructLearnerInsight;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class LearnerInsightAnalysisServiceTest {

    @Mock
    LearnerInsightFinder learnerInsightFinder;

    @Mock
    EventProcessingTracker eventProcessingTracker;

    @Mock
    LearnerInsightLlm learnerInsightLlm;

    @Mock
    LearnerInsightSuggestionService learnerInsightSuggestionService;

    @InjectMocks
    LearnerInsightAnalysisService learnerInsightAnalysisService;

    @Test
    void Evidence_자료로_관련_학습자_인사이트를_조회하고_Reference_자료와_함께_LLM_분석한다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand(List.of(
                new LearnerInsightAnalysisMaterial(EVIDENCE, MESSAGE_CONTENT),
                new LearnerInsightAnalysisMaterial(REFERENCE, COACHING_RESPONSE_CONTENT),
                new LearnerInsightAnalysisMaterial(EVIDENCE, ANOTHER_EVIDENCE_CONTENT)
        ));
        List<String> evidenceContents = List.of(MESSAGE_CONTENT, ANOTHER_EVIDENCE_CONTENT);
        List<String> referenceContents = List.of(COACHING_RESPONSE_CONTENT);
        List<LearnerInsight> currentInsights = List.of(reconstructLearnerInsight());
        LearnerInsightAnalysisRequestData analysisRequest = new LearnerInsightAnalysisRequestData(
                evidenceContents,
                referenceContents,
                currentInsights
        );
        LearnerInsightAnalysisResultData analysisResult = new LearnerInsightAnalysisResultData(List.of());
        given(eventProcessingTracker.isProcessed(command.eventId(), LEARNER_INSIGHT)).willReturn(false);
        given(learnerInsightFinder.findRelevantInsights(
                command.userId(),
                String.join("\n\n", evidenceContents),
                Set.of(CANDIDATE, ACTIVE),
                ANALYSIS_TARGET_MAX_COUNT
        )).willReturn(currentInsights);
        given(learnerInsightLlm.analyze(analysisRequest)).willReturn(analysisResult);

        learnerInsightAnalysisService.analyze(command);

        then(learnerInsightLlm).should().analyze(analysisRequest);
        then(learnerInsightSuggestionService).should().apply(command, analysisResult);
    }

    @Test
    void 이미_처리한_이벤트로_학습자_인사이트를_중복_분석하지_않는다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        given(eventProcessingTracker.isProcessed(command.eventId(), LEARNER_INSIGHT)).willReturn(true);

        learnerInsightAnalysisService.analyze(command);

        then(learnerInsightFinder).shouldHaveNoInteractions();
        then(learnerInsightLlm).shouldHaveNoInteractions();
        then(learnerInsightSuggestionService).shouldHaveNoInteractions();
    }
}
