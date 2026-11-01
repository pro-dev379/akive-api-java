package net.akive.api.learner.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.eventprocessing.application.provided.EventProcessingTracker;
import net.akive.api.learner.application.provided.LearnerInsightAnalyzer;
import net.akive.api.learner.application.provided.LearnerInsightFinder;
import net.akive.api.learner.application.provided.dto.AnalysisMaterialRole;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisMaterial;
import net.akive.api.learner.application.required.LearnerInsightLlm;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisRequestData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.legal.application.provided.LegalRequirementChecker;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static net.akive.api.eventprocessing.domain.EventConsumer.LEARNER_INSIGHT;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.EVIDENCE;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.REFERENCE;
import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;
import static net.akive.api.learner.domain.LearnerPolicy.ANALYSIS_TARGET_MAX_COUNT;
import static net.akive.api.legal.domain.LegalRequirementScope.AI_FEATURE;

@Service
@RequiredArgsConstructor
class LearnerInsightAnalysisService implements LearnerInsightAnalyzer {

    private static final Set<InsightStatus> ANALYSIS_TARGET_STATUSES = Set.of(CANDIDATE, ACTIVE);

    private final LearnerInsightFinder learnerInsightFinder;
    private final EventProcessingTracker eventProcessingTracker;
    private final LegalRequirementChecker legalRequirementChecker;
    private final LearnerInsightLlm learnerInsightLlm;

    private final LearnerInsightSuggestionService learnerInsightSuggestionService;

    @Override
    public void analyze(LearnerInsightAnalysisCommand command) {
        if (eventProcessingTracker.isProcessed(command.eventId(), LEARNER_INSIGHT)) {
            return;
        }

        if (!legalRequirementChecker.check(command.userId(), AI_FEATURE).satisfied()) {
            eventProcessingTracker.markProcessed(command.eventId(), command.eventType(), LEARNER_INSIGHT);
            return;
        }

        List<String> evidenceContents = findMaterialContents(command, EVIDENCE);
        List<LearnerInsight> currentInsights = learnerInsightFinder.findRelevantInsights(
                command.userId(),
                String.join("\n\n", evidenceContents),
                ANALYSIS_TARGET_STATUSES,
                ANALYSIS_TARGET_MAX_COUNT
        );
        LearnerInsightAnalysisRequestData analysisRequest = new LearnerInsightAnalysisRequestData(
                evidenceContents,
                findMaterialContents(command, REFERENCE),
                currentInsights
        );

        LearnerInsightAnalysisResultData analysisResult = learnerInsightLlm.analyze(analysisRequest);

        learnerInsightSuggestionService.apply(command, analysisResult);
    }

    private List<String> findMaterialContents(
            LearnerInsightAnalysisCommand command,
            AnalysisMaterialRole role
    ) {
        return command.materials()
                .stream()
                .filter(material -> material.role() == role)
                .map(LearnerInsightAnalysisMaterial::content)
                .toList();
    }
}
