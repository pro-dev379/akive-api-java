package net.akive.api.learner.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.learner.domain.InsightConfidence.*;
import static net.akive.api.learner.domain.InsightStatus.*;
import static net.akive.api.learner.domain.InsightType.BEHAVIOR_PATTERN;
import static net.akive.api.learner.domain.fixture.LearnerInsightFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.createReconstructedUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LearnerInsightTest {

    @Test
    void 저장된_학습자_인사이트를_복원한다() {
        User user = createReconstructedUser();

        LearnerInsight learnerInsight = reconstructLearnerInsight(
                LEARNER_INSIGHT_ID,
                user,
                BEHAVIOR_PATTERN,
                "  " + INSIGHT_CONTENT + "  ",
                LOW,
                CANDIDATE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );

        assertThat(learnerInsight.getLearnerInsightId()).isEqualTo(LEARNER_INSIGHT_ID);
        assertThat(learnerInsight.getUser()).isEqualTo(user);
        assertThat(learnerInsight.getInsightType()).isEqualTo(BEHAVIOR_PATTERN);
        assertThat(learnerInsight.getContent()).isEqualTo(INSIGHT_CONTENT);
        assertThat(learnerInsight.getConfidence()).isEqualTo(LOW);
        assertThat(learnerInsight.getStatus()).isEqualTo(CANDIDATE);
        assertThat(learnerInsight.getFirstObservedAt()).isEqualTo(FIRST_OBSERVED_AT);
        assertThat(learnerInsight.getLastObservedAt()).isEqualTo(LAST_OBSERVED_AT);
    }

    @Test
    void 학습자_인사이트의_마지막_관찰_시각이_최초_관찰_시각보다_빠르면_학습자_인사이트를_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructLearnerInsight(
                        LEARNER_INSIGHT_ID,
                        createReconstructedUser(),
                        BEHAVIOR_PATTERN,
                        INSIGHT_CONTENT,
                        LOW,
                        CANDIDATE,
                        FIRST_OBSERVED_AT,
                        FIRST_OBSERVED_AT.minusSeconds(1)
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEARNER_INSIGHT_OBSERVATION_PERIOD);
    }

    @Test
    void 후보_상태인_학습자_인사이트는_낮은_신뢰도가_아니면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructLearnerInsight(MEDIUM, CANDIDATE))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEARNER_INSIGHT_STATUS_AND_CONFIDENCE);
    }

    @Test
    void 활성_상태인_학습자_인사이트는_낮은_신뢰도이면_복원할_수_없다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> reconstructLearnerInsight(LOW, ACTIVE))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEARNER_INSIGHT_STATUS_AND_CONFIDENCE);
    }

    @Test
    void 학습자_인사이트를_생성한다() {
        User user = createReconstructedUser();

        LearnerInsight learnerInsight = LearnerInsight.create(
                user,
                BEHAVIOR_PATTERN,
                "  " + INSIGHT_CONTENT + "  ",
                FIRST_OBSERVED_AT
        );

        assertThat(learnerInsight.getLearnerInsightId()).isNull();
        assertThat(learnerInsight.getUser()).isEqualTo(user);
        assertThat(learnerInsight.getInsightType()).isEqualTo(BEHAVIOR_PATTERN);
        assertThat(learnerInsight.getContent()).isEqualTo(INSIGHT_CONTENT);
        assertThat(learnerInsight.getConfidence()).isEqualTo(LOW);
        assertThat(learnerInsight.getStatus()).isEqualTo(CANDIDATE);
        assertThat(learnerInsight.getFirstObservedAt()).isEqualTo(FIRST_OBSERVED_AT);
        assertThat(learnerInsight.getLastObservedAt()).isEqualTo(FIRST_OBSERVED_AT);
    }

    @Test
    void 후보_상태인_학습자_인사이트가_강화되면_신뢰도가_한_단계_높아지고_활성_상태가_된다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(LOW, CANDIDATE);

        learnerInsight.reinforce();

        assertThat(learnerInsight.getConfidence()).isEqualTo(MEDIUM);
        assertThat(learnerInsight.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void 활성_상태인_학습자_인사이트가_강화되면_신뢰도가_높아진다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(MEDIUM, ACTIVE);

        learnerInsight.reinforce();

        assertThat(learnerInsight.getConfidence()).isEqualTo(HIGH);
        assertThat(learnerInsight.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void 신뢰도가_높은_학습자_인사이트는_강화되도_신뢰도가_변하지_않는다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(HIGH, ACTIVE);

        learnerInsight.reinforce();

        assertThat(learnerInsight.getConfidence()).isEqualTo(HIGH);
        assertThat(learnerInsight.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void 신뢰도가_높은_활성_상태의_학습자_인사이트를_반박하면_신뢰도가_한_단계_낮아진다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(HIGH, ACTIVE);

        learnerInsight.contradict();

        assertThat(learnerInsight.getConfidence()).isEqualTo(MEDIUM);
        assertThat(learnerInsight.getStatus()).isEqualTo(ACTIVE);
    }

    @Test
    void 신뢰도가_중간인_활성_상태의_학습자_인사이트를_반박하면_후보_상태로_되돌아_간다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(MEDIUM, ACTIVE);

        learnerInsight.contradict();

        assertThat(learnerInsight.getConfidence()).isEqualTo(LOW);
        assertThat(learnerInsight.getStatus()).isEqualTo(CANDIDATE);
    }

    @Test
    void 학습자_인사이트를_대체_상태로_변경한다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(MEDIUM, ACTIVE);

        learnerInsight.supersede();

        assertThat(learnerInsight.getStatus()).isEqualTo(SUPERSEDED);
        assertThat(learnerInsight.getConfidence()).isEqualTo(MEDIUM);
    }

    @Test
    void 대체_상태된_학습자_인사이트는_강화될_수_없다() {
        LearnerInsight learnerInsight = reconstructSupersededLearnerInsight();

        assertInvalidStatusTransition(learnerInsight::reinforce);
    }

    @Test
    void 대체_상태된_학습자_인사이트는_반박할_수_없다() {
        LearnerInsight learnerInsight = reconstructSupersededLearnerInsight();

        assertInvalidStatusTransition(learnerInsight::contradict);
    }

    @Test
    void 대체_상태된_학습자_인사이트를_다시_대체할_수_없다() {
        LearnerInsight learnerInsight = reconstructSupersededLearnerInsight();

        assertInvalidStatusTransition(learnerInsight::supersede);
    }

    @Test
    void 신뢰도가_낮으면서_후보_상태인_학습자_인사이트를_직접_활성화할_수_없다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(LOW, CANDIDATE);

        assertInvalidStatusTransition(learnerInsight::activate);
    }

    @Test
    void 신뢰도가_중간이면서_활성_상태인_학습자_인사이트를_직접_후보_상태로_되돌릴_수_없다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight(MEDIUM, ACTIVE);

        assertInvalidStatusTransition(learnerInsight::reconsider);
    }

    @Test
    void 같은_식별자를_가진_학습자_인사이트는_같게_취급한다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight();
        LearnerInsight sameLearnerInsight = reconstructLearnerInsight(
                LEARNER_INSIGHT_ID,
                createReconstructedUser(),
                BEHAVIOR_PATTERN,
                "다른 내용",
                MEDIUM,
                ACTIVE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );

        assertThat(learnerInsight).isEqualTo(sameLearnerInsight);
        assertThat(learnerInsight.hashCode()).isEqualTo(sameLearnerInsight.hashCode());
    }

    @Test
    void 식별자가_다른_학습자_인사이트는_다르게_취급한다() {
        LearnerInsight learnerInsight = reconstructLearnerInsight();
        LearnerInsight anotherLearnerInsight = reconstructLearnerInsight(
                ANOTHER_LEARNER_INSIGHT_ID,
                createReconstructedUser(),
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                LOW,
                CANDIDATE,
                FIRST_OBSERVED_AT,
                LAST_OBSERVED_AT
        );

        assertThat(learnerInsight).isNotEqualTo(anotherLearnerInsight);
    }

    @Test
    void 식별자가_없는_학습자_인사이트끼리는_나머지_값이_같더라도_다르게_취급한다() {
        User user = createReconstructedUser();
        LearnerInsight learnerInsight = LearnerInsight.create(
                user,
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                FIRST_OBSERVED_AT
        );
        LearnerInsight anotherLearnerInsight = LearnerInsight.create(
                user,
                BEHAVIOR_PATTERN,
                INSIGHT_CONTENT,
                FIRST_OBSERVED_AT
        );

        assertThat(learnerInsight).isNotEqualTo(anotherLearnerInsight);
    }

    private void assertInvalidStatusTransition(Runnable transition) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(transition::run)
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(INVALID_LEARNER_INSIGHT_STATUS_TRANSITION);
    }
}
