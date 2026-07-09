package net.akive.api.mentoring.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
        MentoringProfile defaultMentoringProfile = MentoringProfile.createDefault(userId);

        // then
        assertThat(defaultMentoringProfile.getMentoringProfileId()).isNull();
        assertThat(defaultMentoringProfile.getUserId()).isEqualTo(userId);
        assertThat(defaultMentoringProfile.getGoal()).isNull();
        assertThat(defaultMentoringProfile.getPreference()).isNull();
        assertThat(defaultMentoringProfile.getConstraint()).isNull();
        assertThat(defaultMentoringProfile.getWeakness()).isNull();
        assertThat(defaultMentoringProfile.getStrength()).isNull();
        assertThat(defaultMentoringProfile.getRecurringPattern()).isNull();
        assertThat(defaultMentoringProfile.getDecisionOrCommitment()).isNull();
        assertThat(defaultMentoringProfile.getStudyPlan()).isNull();
        assertThat(defaultMentoringProfile.getBehavioralPattern()).isNull();
        assertThat(defaultMentoringProfile.getEffectiveAdvice()).isNull();
    }

    @Test
    void 기본_멘토링_프로필은_회원_식별자가_없으면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> MentoringProfile.createDefault(null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 멘토링_프로필_필드가_허용_길이를_넘으면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> MentoringProfile.create(
                        1L,
                        2L,
                        "a".repeat(141),
                        "선호",
                        "제약",
                        "약점",
                        "강점",
                        "반복 패턴",
                        "결정과 약속",
                        "학습 계획",
                        "행동 패턴",
                        "효과적인 조언"
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}