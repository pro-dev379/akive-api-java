package net.akive.api.learner.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.id.UuidGenerator;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.eventprocessing.application.provided.IntegrationEventRecorder;
import net.akive.api.eventprocessing.application.provided.EventProcessingTracker;
import net.akive.api.learner.application.event.LearnerInsightChangedEvent;
import net.akive.api.learner.application.exception.InvalidLearnerInsightAnalysisResultException;
import net.akive.api.learner.application.exception.LearnerInsightAnalysisException;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import net.akive.api.learner.application.provided.dto.LearnerInsightEvidenceSource;
import net.akive.api.learner.application.required.LearnerInsightRepository;
import net.akive.api.learner.application.required.data.InsightSuggestionData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;
import net.akive.api.learner.domain.EvidenceStance;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.learner.domain.LearnerInsightEvidence;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.akive.api.eventprocessing.domain.EventConsumer.LEARNER_INSIGHT;
import static net.akive.api.learner.domain.EvidenceStance.CONTRADICTING;
import static net.akive.api.learner.domain.EvidenceStance.SUPPORTING;
import static net.akive.api.learner.domain.InsightStatus.ACTIVE;
import static net.akive.api.learner.domain.InsightStatus.CANDIDATE;

@Service
@Transactional
@RequiredArgsConstructor
class LearnerInsightSuggestionService {

    private static final Set<InsightStatus> ANALYSIS_TARGET_STATUSES = Set.of(CANDIDATE, ACTIVE);

    private final UserFinder userFinder;

    private final LearnerInsightRepository learnerInsightRepository;
    private final EventProcessingTracker eventProcessingTracker;
    private final IntegrationEventRecorder integrationEventRecorder;

    private final UuidGenerator uuidGenerator;
    private final CurrentTimeProvider currentTimeProvider;

    public void apply(LearnerInsightAnalysisCommand command, LearnerInsightAnalysisResultData analysisResult) {
        if (eventProcessingTracker.isProcessed(command.eventId(), LEARNER_INSIGHT)) {
            return;
        }

        Map<Long, LearnerInsight> analysisTargetById = findAnalysisTargetById(command.userId());
        User user = findUserOrThrow(command.userId());
        Set<Long> processedAnalysisTargetIds = new HashSet<>();

        for (InsightSuggestionData suggestion : analysisResult.suggestions()) {
            applySuggestionByAction(
                    command.evidenceSource(),
                    suggestion,
                    analysisTargetById,
                    processedAnalysisTargetIds,
                    user
            );
        }

        eventProcessingTracker.markProcessed(command.eventId(), command.eventType(), LEARNER_INSIGHT);
    }

    private Map<Long, LearnerInsight> findAnalysisTargetById(Long userId) {
        return learnerInsightRepository
                .findAllByUserIdAndStatusIn(userId, ANALYSIS_TARGET_STATUSES)
                .stream()
                .collect(Collectors.toMap(LearnerInsight::getLearnerInsightId, Function.identity()));
    }

    private User findUserOrThrow(Long userId) {
        return userFinder.find(userId)
                .orElseThrow(() -> new LearnerInsightAnalysisException(
                        "Failed to apply learner insight suggestions because the user could not be found."
                ));
    }

    private void applySuggestionByAction(
            LearnerInsightEvidenceSource evidenceSource,
            InsightSuggestionData suggestion,
            Map<Long, LearnerInsight> analysisTargetById,
            Set<Long> processedAnalysisTargetIds,
            User user
    ) {
        if (suggestion == null || suggestion.action() == null) {
            throw invalidAnalysisResult("suggestion action is required");
        }

        switch (suggestion.action()) {
            case CREATE -> createInsight(evidenceSource, suggestion, user);
            case REINFORCE -> updateInsightWithEvidence(
                    evidenceSource,
                    suggestion,
                    analysisTargetById,
                    processedAnalysisTargetIds,
                    SUPPORTING
            );
            case CONTRADICT -> updateInsightWithEvidence(
                    evidenceSource,
                    suggestion,
                    analysisTargetById,
                    processedAnalysisTargetIds,
                    CONTRADICTING
            );
            case SUPERSEDE -> supersedeInsight(
                    evidenceSource,
                    suggestion,
                    analysisTargetById,
                    processedAnalysisTargetIds,
                    user
            );
            case NONE -> {
            }
        }
    }

    private void createInsight(
            LearnerInsightEvidenceSource evidenceSource,
            InsightSuggestionData suggestion,
            User user
    ) {
        validateExpectedStance(suggestion.stance(), SUPPORTING);

        LearnerInsight insight = learnerInsightRepository.save(LearnerInsight.create(
                user,
                requireSuggestionValue(suggestion.insightType(), "insightType"),
                requireSuggestionValue(suggestion.content(), "content"),
                evidenceSource.observedAt()
        ));

        saveEvidence(evidenceSource, suggestion, SUPPORTING, insight);
        recordInsightChanged(insight);
    }

    private void updateInsightWithEvidence(
            LearnerInsightEvidenceSource evidenceSource,
            InsightSuggestionData suggestion,
            Map<Long, LearnerInsight> analysisTargetById,
            Set<Long> processedAnalysisTargetIds,
            EvidenceStance expectedStance
    ) {
        LearnerInsight analysisTarget = findAnalysisTargetOrThrow(
                suggestion.targetInsightId(),
                analysisTargetById,
                processedAnalysisTargetIds
        );

        validateExpectedStance(suggestion.stance(), expectedStance);
        validateEvidenceNotDuplicated(analysisTarget, evidenceSource);

        if (expectedStance == SUPPORTING) {
            analysisTarget.reinforce();
        } else {
            analysisTarget.contradict();
        }

        saveEvidence(evidenceSource, suggestion, expectedStance, analysisTarget);
        learnerInsightRepository.save(analysisTarget);
        recordInsightChanged(analysisTarget);
    }

    private void supersedeInsight(
            LearnerInsightEvidenceSource evidenceSource,
            InsightSuggestionData suggestion,
            Map<Long, LearnerInsight> analysisTargetById,
            Set<Long> processedAnalysisTargetIds,
            User user
    ) {
        LearnerInsight analysisTarget = findAnalysisTargetOrThrow(
                suggestion.targetInsightId(),
                analysisTargetById,
                processedAnalysisTargetIds
        );

        validateExpectedStance(suggestion.stance(), SUPPORTING);
        validateEvidenceNotDuplicated(analysisTarget, evidenceSource);

        LearnerInsight supersedingInsight = learnerInsightRepository.save(LearnerInsight.create(
                user,
                requireSuggestionValue(suggestion.insightType(), "insightType"),
                requireSuggestionValue(suggestion.content(), "content"),
                evidenceSource.observedAt()
        ));

        saveEvidence(evidenceSource, suggestion, CONTRADICTING, analysisTarget);
        saveEvidence(evidenceSource, suggestion, SUPPORTING, supersedingInsight);

        analysisTarget.supersede();

        learnerInsightRepository.save(analysisTarget);

        recordInsightChanged(analysisTarget);
        recordInsightChanged(supersedingInsight);
    }

    private LearnerInsight findAnalysisTargetOrThrow(
            Long targetInsightId,
            Map<Long, LearnerInsight> analysisTargetById,
            Set<Long> processedAnalysisTargetIds
    ) {
        Long requiredTargetInsightId = requireSuggestionValue(targetInsightId, "targetInsightId");
        LearnerInsight analysisTarget = analysisTargetById.get(requiredTargetInsightId);

        if (analysisTarget == null) {
            throw invalidAnalysisResult(
                    "learner insight is not an eligible analysis target: " + requiredTargetInsightId
            );
        }
        if (!processedAnalysisTargetIds.add(requiredTargetInsightId)) {
            throw invalidAnalysisResult(
                    "multiple suggestions target the same learner insight: " + requiredTargetInsightId
            );
        }

        return analysisTarget;
    }

    private void saveEvidence(
            LearnerInsightEvidenceSource evidenceSource,
            InsightSuggestionData suggestion,
            EvidenceStance stance,
            LearnerInsight learnerInsight
    ) {
        learnerInsightRepository.saveEvidence(LearnerInsightEvidence.create(
                learnerInsight,
                evidenceSource.sourceType(),
                evidenceSource.sourceId(),
                stance,
                requireSuggestionValue(suggestion.evidenceContent(), "evidenceContent"),
                evidenceSource.observedAt()
        ));
    }

    private void validateEvidenceNotDuplicated(
            LearnerInsight learnerInsight,
            LearnerInsightEvidenceSource evidenceSource
    ) {
        if (learnerInsightRepository.existsEvidenceByLearnerInsightIdAndSourceTypeAndSourceId(
                learnerInsight.getLearnerInsightId(),
                evidenceSource.sourceType(),
                evidenceSource.sourceId()
        )) {
            throw invalidAnalysisResult(
                    "evidence already exists for the learner insight and source"
            );
        }
    }

    private void validateExpectedStance(EvidenceStance actualStance, EvidenceStance expectedStance) {
        if (actualStance != expectedStance) {
            throw invalidAnalysisResult(
                    "unexpected evidence stance: expected=" + expectedStance + ", actual=" + actualStance
            );
        }
    }

    private <T> T requireSuggestionValue(T value, String fieldName) {
        if (value == null) {
            throw invalidAnalysisResult("suggestion field is required: " + fieldName);
        }

        return value;
    }

    private InvalidLearnerInsightAnalysisResultException invalidAnalysisResult(String reason) {
        return new InvalidLearnerInsightAnalysisResultException(
                "Invalid learner insight analysis result: " + reason
        );
    }

    private void recordInsightChanged(LearnerInsight learnerInsight) {
        integrationEventRecorder.record(new LearnerInsightChangedEvent(
                uuidGenerator.generate(),
                learnerInsight.getUser().getUserId(),
                learnerInsight.getLearnerInsightId(),
                learnerInsight.getInsightType(),
                learnerInsight.getContent(),
                learnerInsight.getConfidence(),
                learnerInsight.getStatus(),
                currentTimeProvider.now()
        ));
    }
}
