package net.akive.api.mentoring.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.mentoring.domain.MentoringFixture.createMentoringProfile;
import static net.akive.api.mentoring.domain.MentoringFixture.createMentoringProfileWithDetail;
import static net.akive.api.mentoring.domain.MentoringPolicy.GOAL_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MentoringProfileTest {

    @Test
    void 멘토링_프로필을_생성한다() {
        MentoringProfile defaultMentoringProfile = createMentoringProfile();

        assertThat(defaultMentoringProfile.getUser()).isNotNull();
        assertThat(defaultMentoringProfile.getDetail()).isNull();
    }

    @Test
    void 멘토링_프로필을_업데이트_한다() {
        MentoringProfile mentoringProfile = createMentoringProfileWithDetail();
        MentoringProfileUpdateInfo updateInfo = new MentoringProfileUpdateInfo(
                "업데이트된 목표",
                "업데이트된 선호",
                "업데이트된 제약",
                "업데이트된 약점",
                "업데이트된 강점",
                "업데이트된 반복 패턴",
                "업데이트된 결정과 약속",
                "업데이트된 학습 계획",
                "업데이트된 행동 패턴",
                "업데이트된 효과적인 조언"
        );

        mentoringProfile.update(updateInfo);

        assertThat(mentoringProfile.getDetail().goal()).isEqualTo(updateInfo.goal());
        assertThat(mentoringProfile.getDetail().preference()).isEqualTo(updateInfo.preference());
        assertThat(mentoringProfile.getDetail().constraint()).isEqualTo(updateInfo.constraint());
        assertThat(mentoringProfile.getDetail().weakness()).isEqualTo(updateInfo.weakness());
        assertThat(mentoringProfile.getDetail().strength()).isEqualTo(updateInfo.strength());
        assertThat(mentoringProfile.getDetail().recurringPattern()).isEqualTo(updateInfo.recurringPattern());
        assertThat(mentoringProfile.getDetail().decisionOrCommitment()).isEqualTo(updateInfo.decisionOrCommitment());
        assertThat(mentoringProfile.getDetail().studyPlan()).isEqualTo(updateInfo.studyPlan());
        assertThat(mentoringProfile.getDetail().behavioralPattern()).isEqualTo(updateInfo.behavioralPattern());
        assertThat(mentoringProfile.getDetail().effectiveAdvice()).isEqualTo(updateInfo.effectiveAdvice());
    }

    @Test
    void 업데이트_시_글자_길이를_초과하는_값이_있으면_업데이트_실패한다() {
        MentoringProfile mentoringProfile = createMentoringProfileWithDetail();
        MentoringProfileUpdateInfo updateInfo = new MentoringProfileUpdateInfo(
                "a".repeat(GOAL_MAX_LENGTH + 1),
                "선호",
                "제약",
                "약점",
                "강점",
                "반복 패턴",
                "결정과 약속",
                "학습 계획",
                "행동 패턴",
                "효과적인 조언"
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> mentoringProfile.update(updateInfo))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
