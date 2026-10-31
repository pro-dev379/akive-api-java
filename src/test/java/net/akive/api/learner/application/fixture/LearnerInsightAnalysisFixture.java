package net.akive.api.learner.application.fixture;

import net.akive.api.eventprocessing.domain.EventType;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisCommand;
import net.akive.api.learner.application.provided.dto.LearnerInsightAnalysisMaterial;
import net.akive.api.learner.application.provided.dto.LearnerInsightEvidenceSource;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisRequestData;
import net.akive.api.learner.application.required.data.LearnerInsightAnalysisResultData;
import net.akive.api.learner.application.required.data.InsightSuggestionData;

import java.util.List;
import java.util.UUID;

import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.COACHING_RESPONSE_CONTENT;
import static net.akive.api.coaching.application.fixture.CoachingMessageCommandFixture.EVENT_ID;
import static net.akive.api.coaching.application.fixture.CoachingTurnFixture.COACHING_TURN_COMPLETED_AT;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.COACHING_MESSAGE_ID;
import static net.akive.api.coaching.domain.fixture.CoachingMessageFixture.MESSAGE_CONTENT;
import static net.akive.api.eventprocessing.domain.EventType.COACHING_TURN_DONE;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.EVIDENCE;
import static net.akive.api.learner.application.provided.dto.AnalysisMaterialRole.REFERENCE;
import static net.akive.api.learner.domain.EvidenceSourceType.COACHING_MESSAGE;
import static net.akive.api.learner.domain.EvidenceStance.CONTRADICTING;
import static net.akive.api.learner.domain.EvidenceStance.SUPPORTING;
import static net.akive.api.learner.domain.InsightType.BEHAVIOR_PATTERN;
import static net.akive.api.learner.domain.InsightType.STRENGTH;
import static net.akive.api.learner.application.required.data.InsightAction.*;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.ANOTHER_INSIGHT_CONTENT;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.LEARNER_INSIGHT_ID;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.reconstructLearnerInsight;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public final class LearnerInsightAnalysisFixture {

    public static final String ANALYSIS_EVIDENCE_CONTENT = "회원은 큰 계획보다 작은 계획을 꾸준히 실천한다고 말했습니다.";
    public static final String SUGGESTION_EVIDENCE_CONTENT = "작은 계획을 꾸준히 실천한다고 말했다.";

    private LearnerInsightAnalysisFixture() {
    }

    public static LearnerInsightAnalysisRequestData createLearnerInsightAnalysisRequestData() {
        return new LearnerInsightAnalysisRequestData(
                List.of(ANALYSIS_EVIDENCE_CONTENT),
                List.of(),
                List.of(reconstructLearnerInsight())
        );
    }

    public static LearnerInsightAnalysisResultData createLearnerInsightAnalysisResultData(
            InsightSuggestionData... suggestions
    ) {
        return new LearnerInsightAnalysisResultData(List.of(suggestions));
    }

    public static InsightSuggestionData createCreateSuggestion() {
        return new InsightSuggestionData(
                CREATE,
                null,
                BEHAVIOR_PATTERN,
                ANOTHER_INSIGHT_CONTENT,
                SUPPORTING,
                SUGGESTION_EVIDENCE_CONTENT
        );
    }

    public static InsightSuggestionData createReinforceSuggestion() {
        return new InsightSuggestionData(
                REINFORCE,
                LEARNER_INSIGHT_ID,
                null,
                null,
                SUPPORTING,
                SUGGESTION_EVIDENCE_CONTENT
        );
    }

    public static InsightSuggestionData createContradictSuggestion() {
        return new InsightSuggestionData(
                CONTRADICT,
                LEARNER_INSIGHT_ID,
                null,
                null,
                CONTRADICTING,
                SUGGESTION_EVIDENCE_CONTENT
        );
    }

    public static InsightSuggestionData createSupersedeSuggestion() {
        return new InsightSuggestionData(
                SUPERSEDE,
                LEARNER_INSIGHT_ID,
                STRENGTH,
                ANOTHER_INSIGHT_CONTENT,
                SUPPORTING,
                SUGGESTION_EVIDENCE_CONTENT
        );
    }

    public static InsightSuggestionData createNoneSuggestion() {
        return new InsightSuggestionData(NONE, null, null, null, null, null);
    }

    public static LearnerInsightAnalysisCommand createLearnerInsightAnalysisCommand() {
        return createLearnerInsightAnalysisCommand(EVENT_ID, COACHING_TURN_DONE);
    }

    public static LearnerInsightAnalysisCommand createLearnerInsightAnalysisCommand(
            UUID eventId,
            EventType eventType
    ) {
        return createLearnerInsightAnalysisCommand(
                eventId,
                eventType,
                List.of(
                        new LearnerInsightAnalysisMaterial(EVIDENCE, MESSAGE_CONTENT),
                        new LearnerInsightAnalysisMaterial(REFERENCE, COACHING_RESPONSE_CONTENT)
                )
        );
    }

    public static LearnerInsightAnalysisCommand createLearnerInsightAnalysisCommand(
            List<LearnerInsightAnalysisMaterial> materials
    ) {
        return createLearnerInsightAnalysisCommand(EVENT_ID, COACHING_TURN_DONE, materials);
    }

    private static LearnerInsightAnalysisCommand createLearnerInsightAnalysisCommand(
            UUID eventId,
            EventType eventType,
            List<LearnerInsightAnalysisMaterial> materials
    ) {
        return new LearnerInsightAnalysisCommand(
                eventId,
                eventType,
                USER_ID,
                new LearnerInsightEvidenceSource(
                        COACHING_MESSAGE,
                        COACHING_MESSAGE_ID,
                        COACHING_TURN_COMPLETED_AT
                ),
                materials
        );
    }
}
