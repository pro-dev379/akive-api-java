package net.akive.api.mentoring.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MentoringProfileTest {

    @Test
    void 멘토링_프로필을_생성한다() {
        // given
        Long mentoringProfileId = 1L;
        Long userId = 2L;

        // when
        MentoringProfile mentoringProfile = MentoringProfile.create(
                mentoringProfileId,
                userId,
                "목표",
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

        // then
        assertThat(mentoringProfile.getMentoringProfileId()).isEqualTo(mentoringProfileId);
        assertThat(mentoringProfile.getUserId()).isEqualTo(userId);
        assertThat(mentoringProfile.getGoal()).isEqualTo("목표");
        assertThat(mentoringProfile.getPreference()).isEqualTo("선호");
        assertThat(mentoringProfile.getConstraint()).isEqualTo("제약");
        assertThat(mentoringProfile.getWeakness()).isEqualTo("약점");
        assertThat(mentoringProfile.getStrength()).isEqualTo("강점");
        assertThat(mentoringProfile.getRecurringPattern()).isEqualTo("반복 패턴");
        assertThat(mentoringProfile.getDecisionOrCommitment()).isEqualTo("결정과 약속");
        assertThat(mentoringProfile.getStudyPlan()).isEqualTo("학습 계획");
        assertThat(mentoringProfile.getBehavioralPattern()).isEqualTo("행동 패턴");
        assertThat(mentoringProfile.getEffectiveAdvice()).isEqualTo("효과적인 조언");
    }

    @Test
    void 기본_멘토링_프로필을_생성한다() {
        // given
        Long userId = 1L;

        // when
        MentoringProfile mentoringProfile = MentoringProfile.createDefault(userId);

        // then
        assertThat(mentoringProfile.getUserId()).isEqualTo(userId);
    }
}