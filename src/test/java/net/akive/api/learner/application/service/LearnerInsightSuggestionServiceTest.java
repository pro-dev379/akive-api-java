package net.akive.api.learner.application.service;

import net.akive.api.common.id.UuidGenerator;
import net.akive.api.common.time.CurrentTimeProvider;
import net.akive.api.eventprocessing.application.provided.EventProcessingTracker;
import net.akive.api.eventprocessing.application.provided.IntegrationEventRecorder;
import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.learner.application.event.LearnerInsightChangedEvent;
import net.akive.api.learner.application.exception.InvalidLearnerInsightAnalysisResultException;
import net.akive.api.learner.application.exception.LearnerInsightAnalysisException;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import net.akive.api.learner.application.provided.dto.LearnerInsightEvidenceSource;
import net.akive.api.learner.application.required.LearnerInsightRepository;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;
import net.akive.api.learner.domain.EvidenceStance;
import net.akive.api.learner.domain.InsightStatus;
import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.learner.domain.LearnerInsightEvidence;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static net.akive.api.eventprocessing.domain.EventConsumer.LEARNER_INSIGHT;
import static net.akive.api.learner.application.fixture.LearnerInsightAnalysisFixture.*;
import static net.akive.api.learner.application.fixture.LearnerInsightEventFixture.*;
import static net.akive.api.learner.domain.EvidenceStance.CONTRADICTING;
import static net.akive.api.learner.domain.EvidenceStance.SUPPORTING;
import static net.akive.api.learner.domain.InsightConfidence.LOW;
import static net.akive.api.learner.domain.InsightConfidence.MEDIUM;
import static net.akive.api.learner.domain.InsightStatus.*;
import static net.akive.api.learner.domain.InsightType.BEHAVIOR_PATTERN;
import static net.akive.api.learner.domain.InsightType.STRENGTH;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LearnerInsightSuggestionServiceTest {

    private static final Set<InsightStatus> ANALYSIS_TARGET_STATUSES = Set.of(CANDIDATE, ACTIVE);

    @Mock
    UserFinder userFinder;

    @Mock
    LearnerInsightRepository learnerInsightRepository;

    @Mock
    EventProcessingTracker eventProcessingTracker;

    @Mock
    IntegrationEventRecorder integrationEventRecorder;

    @Mock
    UuidGenerator uuidGenerator;

    @Mock
    CurrentTimeProvider currentTimeProvider;

    @InjectMocks
    LearnerInsightSuggestionService learnerInsightSuggestionService;

    @Test
    void CREATE_제안으로_학습자_인사이트와_근거를_저장하고_변경_이벤트를_기록한다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        User user = createReconstructedUser();
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createCreateSuggestion());
        LearnerInsight savedInsight = reconstructLearnerInsight(
                LEARNER_INSIGHT_ID,
                user,
                BEHAVIOR_PATTERN,
                ANOTHER_INSIGHT_CONTENT,
                LOW,
                CANDIDATE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );
        prepareSuggestionApplication(command, user, List.of());
        given(learnerInsightRepository.save(any(LearnerInsight.class))).willReturn(savedInsight);
        prepareChangedEventRecording(LEARNER_INSIGHT_CHANGED_EVENT_ID);

        learnerInsightSuggestionService.apply(command, analysisResult);

        ArgumentCaptor<LearnerInsight> insightCaptor = ArgumentCaptor.forClass(LearnerInsight.class);
        then(learnerInsightRepository).should().save(insightCaptor.capture());
        LearnerInsight createdInsight = insightCaptor.getValue();
        assertThat(createdInsight.getUser()).isEqualTo(user);
        assertThat(createdInsight.getInsightType()).isEqualTo(BEHAVIOR_PATTERN);
        assertThat(createdInsight.getContent()).isEqualTo(ANOTHER_INSIGHT_CONTENT);
        assertThat(createdInsight.getConfidence()).isEqualTo(LOW);
        assertThat(createdInsight.getStatus()).isEqualTo(CANDIDATE);
        assertThat(createdInsight.getFirstObservedAt()).isEqualTo(command.evidenceSource().observedAt());
        assertThat(createdInsight.getLastObservedAt()).isEqualTo(command.evidenceSource().observedAt());
        assertSavedEvidence(command.evidenceSource(), savedInsight, SUPPORTING);
        assertChangedEventRecorded(LEARNER_INSIGHT_CHANGED_EVENT_ID, savedInsight);
        assertEventProcessed(command.eventId(), command.eventType());
    }

    @Test
    void REINFORCE_제안으로_기존_학습자_인사이트를_강화하고_근거와_변경_이벤트를_기록한다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        User user = createReconstructedUser();
        LearnerInsight analysisTarget = reconstructLearnerInsight(LOW, CANDIDATE);
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createReinforceSuggestion());
        prepareSuggestionApplication(command, user, List.of(analysisTarget));
        prepareChangedEventRecording(LEARNER_INSIGHT_CHANGED_EVENT_ID);

        learnerInsightSuggestionService.apply(command, analysisResult);

        assertThat(analysisTarget.getConfidence()).isEqualTo(MEDIUM);
        assertThat(analysisTarget.getStatus()).isEqualTo(ACTIVE);
        assertThat(analysisTarget.getLastObservedAt()).isEqualTo(command.evidenceSource().observedAt());
        then(learnerInsightRepository).should().save(analysisTarget);
        assertSavedEvidence(command.evidenceSource(), analysisTarget, SUPPORTING);
        assertChangedEventRecorded(LEARNER_INSIGHT_CHANGED_EVENT_ID, analysisTarget);
        assertEventProcessed(command.eventId(), command.eventType());
    }

    @Test
    void CONTRADICT_제안으로_기존_학습자_인사이트의_신뢰도를_낮추고_근거와_변경_이벤트를_기록한다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        User user = createReconstructedUser();
        LearnerInsight analysisTarget = reconstructLearnerInsight(MEDIUM, ACTIVE);
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createContradictSuggestion());
        prepareSuggestionApplication(command, user, List.of(analysisTarget));
        prepareChangedEventRecording(LEARNER_INSIGHT_CHANGED_EVENT_ID);

        learnerInsightSuggestionService.apply(command, analysisResult);

        assertThat(analysisTarget.getConfidence()).isEqualTo(LOW);
        assertThat(analysisTarget.getStatus()).isEqualTo(CANDIDATE);
        assertThat(analysisTarget.getLastObservedAt()).isEqualTo(command.evidenceSource().observedAt());
        then(learnerInsightRepository).should().save(analysisTarget);
        assertSavedEvidence(command.evidenceSource(), analysisTarget, CONTRADICTING);
        assertChangedEventRecorded(LEARNER_INSIGHT_CHANGED_EVENT_ID, analysisTarget);
        assertEventProcessed(command.eventId(), command.eventType());
    }

    @Test
    void SUPERSEDE_제안으로_기존_학습자_인사이트를_대체하고_각각의_근거와_변경_이벤트를_기록한다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        User user = createReconstructedUser();
        LearnerInsight analysisTarget = reconstructLearnerInsight(LOW, CANDIDATE);
        LearnerInsight supersedingInsight = reconstructLearnerInsight(
                ANOTHER_LEARNER_INSIGHT_ID,
                user,
                STRENGTH,
                ANOTHER_INSIGHT_CONTENT,
                LOW,
                CANDIDATE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createSupersedeSuggestion());
        prepareSuggestionApplication(command, user, List.of(analysisTarget));
        given(learnerInsightRepository.save(argThat(insight -> insight.getLearnerInsightId() == null)))
                .willReturn(supersedingInsight);
        prepareChangedEventRecording(LEARNER_INSIGHT_CHANGED_EVENT_ID, ANOTHER_LEARNER_INSIGHT_CHANGED_EVENT_ID);

        learnerInsightSuggestionService.apply(command, analysisResult);

        assertThat(analysisTarget.getStatus()).isEqualTo(SUPERSEDED);
        then(learnerInsightRepository).should().save(analysisTarget);

        ArgumentCaptor<LearnerInsightEvidence> evidenceCaptor =
                ArgumentCaptor.forClass(LearnerInsightEvidence.class);
        then(learnerInsightRepository).should(times(2))
                .saveEvidence(evidenceCaptor.capture());
        assertEvidence(
                evidenceCaptor.getAllValues().get(0),
                command.evidenceSource(),
                analysisTarget,
                CONTRADICTING
        );
        assertEvidence(
                evidenceCaptor.getAllValues().get(1),
                command.evidenceSource(),
                supersedingInsight,
                SUPPORTING
        );
        assertChangedEventRecorded(LEARNER_INSIGHT_CHANGED_EVENT_ID, analysisTarget);
        assertChangedEventRecorded(ANOTHER_LEARNER_INSIGHT_CHANGED_EVENT_ID, supersedingInsight);
        assertEventProcessed(command.eventId(), command.eventType());
    }

    @Test
    void NONE_제안이면_학습자_인사이트를_변경하지_않고_이벤트_처리_완료_처리한다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        User user = createReconstructedUser();
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createNoneSuggestion());
        prepareSuggestionApplication(command, user, List.of());

        learnerInsightSuggestionService.apply(command, analysisResult);

        then(learnerInsightRepository).should(never()).save(any(LearnerInsight.class));
        then(learnerInsightRepository).should(never()).saveEvidence(any(LearnerInsightEvidence.class));
        then(integrationEventRecorder).shouldHaveNoInteractions();
        assertEventProcessed(command.eventId(), command.eventType());
    }

    @Test
    void 이미_처리한_이벤트의_학습자_인사이트_변경_제안을_중복_반영하지_않는다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createCreateSuggestion());
        given(eventProcessingTracker.isProcessed(command.eventId(), LEARNER_INSIGHT)).willReturn(true);

        learnerInsightSuggestionService.apply(command, analysisResult);

        then(userFinder).shouldHaveNoInteractions();
        then(learnerInsightRepository).shouldHaveNoInteractions();
        then(integrationEventRecorder).shouldHaveNoInteractions();
        then(eventProcessingTracker).should(never()).markProcessed(any(), any(), any());
    }

    @Test
    void 제안_대상_학습자_인사이트가_분석_대상에_없으면_예외를_던진다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        User user = createReconstructedUser();
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createReinforceSuggestion());
        prepareSuggestionApplication(command, user, List.of());

        assertThatExceptionOfType(InvalidLearnerInsightAnalysisResultException.class)
                .isThrownBy(() -> learnerInsightSuggestionService.apply(command, analysisResult))
                .withMessage("Invalid learner insight analysis result: "
                        + "learner insight is not an eligible analysis target: " + LEARNER_INSIGHT_ID);

        then(eventProcessingTracker).should(never()).markProcessed(any(), any(), any());
    }

    @Test
    void 같은_출처의_학습자_인사이트_근거가_이미_있으면_예외를_던진다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        User user = createReconstructedUser();
        LearnerInsight analysisTarget = reconstructLearnerInsight(LOW, CANDIDATE);
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createReinforceSuggestion());
        prepareSuggestionApplication(command, user, List.of(analysisTarget));
        given(learnerInsightRepository.existsEvidenceByLearnerInsightIdAndSourceTypeAndSourceId(
                analysisTarget.getLearnerInsightId(),
                command.evidenceSource().sourceType(),
                command.evidenceSource().sourceId()
        )).willReturn(true);

        assertThatExceptionOfType(InvalidLearnerInsightAnalysisResultException.class)
                .isThrownBy(() -> learnerInsightSuggestionService.apply(command, analysisResult))
                .withMessage("Invalid learner insight analysis result: "
                        + "evidence already exists for the learner insight and source");

        assertThat(analysisTarget.getConfidence()).isEqualTo(LOW);
        assertThat(analysisTarget.getStatus()).isEqualTo(CANDIDATE);
        then(learnerInsightRepository).should(never()).saveEvidence(any(LearnerInsightEvidence.class));
        then(eventProcessingTracker).should(never()).markProcessed(any(), any(), any());
    }

    @Test
    void 회원을_찾을_수_없으면_예외를_던진다() {
        LearnerInsightAnalysisCommand command = createLearnerInsightAnalysisCommand();
        LearnerInsightAnalysisResultData analysisResult =
                createLearnerInsightAnalysisResultData(createCreateSuggestion());
        given(eventProcessingTracker.isProcessed(command.eventId(), LEARNER_INSIGHT)).willReturn(false);
        given(learnerInsightRepository.findAllByUserIdAndStatusIn(
                command.userId(),
                ANALYSIS_TARGET_STATUSES
        )).willReturn(List.of());
        given(userFinder.find(command.userId())).willReturn(Optional.empty());

        assertThatExceptionOfType(LearnerInsightAnalysisException.class)
                .isThrownBy(() -> learnerInsightSuggestionService.apply(command, analysisResult))
                .withMessage("Failed to apply learner insight suggestions because the user could not be found.");

        then(eventProcessingTracker).should(never()).markProcessed(any(), any(), any());
    }

    private void prepareSuggestionApplication(
            LearnerInsightAnalysisCommand command,
            User user,
            List<LearnerInsight> analysisTargets
    ) {
        given(eventProcessingTracker.isProcessed(command.eventId(), LEARNER_INSIGHT)).willReturn(false);
        given(learnerInsightRepository.findAllByUserIdAndStatusIn(
                command.userId(),
                ANALYSIS_TARGET_STATUSES
        )).willReturn(analysisTargets);
        given(userFinder.find(command.userId())).willReturn(Optional.of(user));
    }

    private void prepareChangedEventRecording(UUID... eventIds) {
        given(uuidGenerator.generate()).willReturn(eventIds[0], Arrays.copyOfRange(eventIds, 1, eventIds.length));
        given(currentTimeProvider.now()).willReturn(LEARNER_INSIGHT_CHANGED_AT);
    }

    private void assertSavedEvidence(
            LearnerInsightEvidenceSource expectedSource,
            LearnerInsight expectedLearnerInsight,
            EvidenceStance expectedStance
    ) {
        ArgumentCaptor<LearnerInsightEvidence> evidenceCaptor =
                ArgumentCaptor.forClass(LearnerInsightEvidence.class);
        then(learnerInsightRepository).should().saveEvidence(evidenceCaptor.capture());
        assertEvidence(
                evidenceCaptor.getValue(),
                expectedSource,
                expectedLearnerInsight,
                expectedStance
        );
    }

    private void assertEvidence(
            LearnerInsightEvidence actual,
            LearnerInsightEvidenceSource expectedSource,
            LearnerInsight expectedLearnerInsight,
            EvidenceStance expectedStance
    ) {
        assertThat(actual.getLearnerInsight()).isEqualTo(expectedLearnerInsight);
        assertThat(actual.getSourceType()).isEqualTo(expectedSource.sourceType());
        assertThat(actual.getSourceId()).isEqualTo(expectedSource.sourceId());
        assertThat(actual.getStance()).isEqualTo(expectedStance);
        assertThat(actual.getContent()).isEqualTo(SUGGESTION_EVIDENCE_CONTENT);
    }

    private void assertChangedEventRecorded(UUID eventId, LearnerInsight learnerInsight) {
        then(integrationEventRecorder).should().record(new LearnerInsightChangedEvent(
                eventId,
                learnerInsight.getUser().getUserId(),
                learnerInsight.getLearnerInsightId(),
                learnerInsight.getInsightType(),
                learnerInsight.getContent(),
                learnerInsight.getConfidence(),
                learnerInsight.getStatus(),
                LEARNER_INSIGHT_CHANGED_AT
        ));
    }

    private void assertEventProcessed(UUID eventId, EventType eventType) {
        then(eventProcessingTracker).should().markProcessed(
                eventId,
                eventType,
                LEARNER_INSIGHT
        );
    }
}
