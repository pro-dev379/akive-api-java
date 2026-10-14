package net.akive.api.learner.domain.fixture;

import net.akive.api.learner.domain.LearnerInsight;
import net.akive.api.learner.domain.LearnerInsightEvidence;

import java.time.Instant;

import static net.akive.api.learner.domain.EvidenceSourceType.COACHING_MESSAGE;
import static net.akive.api.learner.domain.EvidenceStance.SUPPORTING;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.LAST_OBSERVED_AT;

public final class LearnerInsightEvidenceFixture {

    public static final Long LEARNER_INSIGHT_EVIDENCE_ID = 1L;
    public static final Long ANOTHER_LEARNER_INSIGHT_EVIDENCE_ID = 2L;
    public static final Long SOURCE_ID = 1L;
    public static final String EVIDENCE_CONTENT = "작은 단위로 계획했을 때 꾸준히 실천할 수 있었다.";
    public static final String ANOTHER_EVIDENCE_CONTENT = "이번에도 할 일을 작게 나누어 모두 완료했다.";
    public static final Instant EVIDENCE_OBSERVED_AT = LAST_OBSERVED_AT.plusSeconds(1);
    public static final Instant CREATED_AT = Instant.parse("2026-10-16T00:00:02Z");

    private LearnerInsightEvidenceFixture() {
    }

    public static LearnerInsightEvidence createLearnerInsightEvidence(
            LearnerInsight learnerInsight,
            Long sourceId
    ) {
        return LearnerInsightEvidence.create(
                learnerInsight,
                COACHING_MESSAGE,
                sourceId,
                SUPPORTING,
                EVIDENCE_CONTENT,
                EVIDENCE_OBSERVED_AT
        );
    }

    public static LearnerInsightEvidence reconstructLearnerInsightEvidence() {
        return reconstructLearnerInsightEvidence(
                LEARNER_INSIGHT_EVIDENCE_ID,
                LearnerInsightFixture.reconstructLearnerInsight()
        );
    }

    public static LearnerInsightEvidence reconstructLearnerInsightEvidence(
            Long learnerInsightEvidenceId,
            LearnerInsight learnerInsight
    ) {
        return LearnerInsightEvidence.reconstruct(
                learnerInsightEvidenceId,
                learnerInsight,
                COACHING_MESSAGE,
                SOURCE_ID,
                SUPPORTING,
                EVIDENCE_CONTENT,
                CREATED_AT
        );
    }
}
