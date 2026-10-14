package net.akive.api.learner.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_LEARNER_INSIGHT_OBSERVATION_PERIOD;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.LEARNER_INSIGHT_EVIDENCE_ADDITION_NOT_ALLOWED;
import static net.akive.api.learner.domain.EvidenceSourceType.COACHING_MESSAGE;
import static net.akive.api.learner.domain.EvidenceStance.CONTRADICTING;
import static net.akive.api.learner.domain.EvidenceStance.SUPPORTING;
import static net.akive.api.learner.domain.fixture.LearnerInsightEvidenceFixture.*;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LearnerInsightEvidenceTest {

    @Test
    void 저장된_학습자_인사이트_근거를_복원한다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight();

        LearnerInsightEvidence evidence = LearnerInsightEvidence.reconstruct(
                LEARNER_INSIGHT_EVIDENCE_ID,
                learnerInsight,
                COACHING_MESSAGE,
                SOURCE_ID,
                SUPPORTING,
                EVIDENCE_CONTENT,
                CREATED_AT
        );

        assertThat(evidence.getLearnerInsightEvidenceId()).isEqualTo(LEARNER_INSIGHT_EVIDENCE_ID);
        assertThat(evidence.getLearnerInsight()).isEqualTo(learnerInsight);
        assertThat(evidence.getSourceType()).isEqualTo(COACHING_MESSAGE);
        assertThat(evidence.getSourceId()).isEqualTo(SOURCE_ID);
        assertThat(evidence.getStance()).isEqualTo(SUPPORTING);
        assertThat(evidence.getContent()).isEqualTo(EVIDENCE_CONTENT);
        assertThat(evidence.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 학습자_인사이트_근거를_생성하고_학습자_인사이트의_마지막_관찰_시각을_갱신한다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight();

        LearnerInsightEvidence evidence = LearnerInsightEvidence.create(
                learnerInsight,
                COACHING_MESSAGE,
                SOURCE_ID,
                SUPPORTING,
                "  " + EVIDENCE_CONTENT + "  ",
                EVIDENCE_OBSERVED_AT
        );

        assertThat(evidence.getLearnerInsightEvidenceId()).isNull();
        assertThat(evidence.getLearnerInsight()).isEqualTo(learnerInsight);
        assertThat(evidence.getSourceType()).isEqualTo(COACHING_MESSAGE);
        assertThat(evidence.getSourceId()).isEqualTo(SOURCE_ID);
        assertThat(evidence.getStance()).isEqualTo(SUPPORTING);
        assertThat(evidence.getContent()).isEqualTo(EVIDENCE_CONTENT);
        assertThat(evidence.getCreatedAt()).isNull();
        assertThat(learnerInsight.getLastObservedAt()).isEqualTo(EVIDENCE_OBSERVED_AT);
    }

    @Test
    void 학습자_인사이트의_마지막_관찰_시각보다_과거에_관찰된_학습자_인사이트_근거를_생성할_수_없다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight();
        Instant lastObservedAtBefore = learnerInsight.getLastObservedAt();
        Instant evidenceObservedAt = learnerInsight.getLastObservedAt().minusSeconds(1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> LearnerInsightEvidence.create(
                        learnerInsight,
                        COACHING_MESSAGE,
                        SOURCE_ID,
                        SUPPORTING,
                        EVIDENCE_CONTENT,
                        evidenceObservedAt
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEARNER_INSIGHT_OBSERVATION_PERIOD);
        assertThat(learnerInsight.getLastObservedAt()).isEqualTo(lastObservedAtBefore);
    }

    @Test
    void 대체된_학습자_인사이트에는_학습자_인사이트_근거를_추가할_수_없다() {
        LearnerInsight learnerInsight = reconstructSupersededLearnerInsight();

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> LearnerInsightEvidence.create(
                        learnerInsight,
                        COACHING_MESSAGE,
                        SOURCE_ID,
                        CONTRADICTING,
                        EVIDENCE_CONTENT,
                        EVIDENCE_OBSERVED_AT
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(LEARNER_INSIGHT_EVIDENCE_ADDITION_NOT_ALLOWED);
        assertThat(learnerInsight.getLastObservedAt()).isEqualTo(LAST_OBSERVED_AT);
    }

    @Test
    void 같은_식별자를_가진_학습자_인사이트_근거는_같게_취급한다() {
        LearnerInsightEvidence evidence = reconstructLearnerInsightEvidence();
        LearnerInsightEvidence sameEvidence = reconstructLearnerInsightEvidence(
                LEARNER_INSIGHT_EVIDENCE_ID,
                reconstructLearnerInsight()
        );

        assertThat(evidence).isEqualTo(sameEvidence);
        assertThat(evidence.hashCode()).isEqualTo(sameEvidence.hashCode());
    }

    @Test
    void 식별자가_다른_학습자_인사이트_근거는_다르게_취급한다() {
        LearnerInsightEvidence evidence = reconstructLearnerInsightEvidence();
        LearnerInsightEvidence anotherEvidence = reconstructLearnerInsightEvidence(
                ANOTHER_LEARNER_INSIGHT_EVIDENCE_ID,
                reconstructLearnerInsight()
        );

        assertThat(evidence).isNotEqualTo(anotherEvidence);
    }

    @Test
    void 식별자가_없는_학습자_인사이트_근거끼리는_나머지_값이_같더라도_다르게_취급한다() {
        LearnerInsight firstLearnerInsight = reconstructLearnerInsight();
        LearnerInsight anotherLearnerInsight = reconstructLearnerInsight();
        LearnerInsightEvidence evidence = LearnerInsightEvidence.create(
                firstLearnerInsight,
                COACHING_MESSAGE,
                SOURCE_ID,
                SUPPORTING,
                EVIDENCE_CONTENT,
                EVIDENCE_OBSERVED_AT
        );
        LearnerInsightEvidence anotherEvidence = LearnerInsightEvidence.create(
                anotherLearnerInsight,
                COACHING_MESSAGE,
                SOURCE_ID,
                SUPPORTING,
                EVIDENCE_CONTENT,
                EVIDENCE_OBSERVED_AT
        );

        assertThat(evidence).isNotEqualTo(anotherEvidence);
    }
}
