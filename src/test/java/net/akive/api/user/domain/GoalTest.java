package net.akive.api.user.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class GoalTest {

    @Test
    void 목표를_생성한다() {
        // given
        Long goalId = 1L;
        Long userId = 2L;
        LocalTime attendOn = LocalTime.of(9, 30);
        long rest10MinSec = 180L;
        int mealNum = 2;
        long meal1TimeSec = 1_800L;

        // when
        Goal goal = Goal.create(
                goalId,
                userId,
                attendOn,
                rest10MinSec,
                mealNum,
                meal1TimeSec
        );

        // then
        assertThat(goal.getGoalId()).isEqualTo(goalId);
        assertThat(goal.getUserId()).isEqualTo(userId);
        assertThat(goal.getAttendOn()).isEqualTo(attendOn);
        assertThat(goal.getRest10MinSec()).isEqualTo(rest10MinSec);
        assertThat(goal.getMealNum()).isEqualTo(mealNum);
        assertThat(goal.getMeal1TimeSec()).isEqualTo(meal1TimeSec);
    }

    @Test
    void 기본_목표를_생성한다() {
        // given
        Long userId = 1L;

        // when
        Goal goal = Goal.createDefault(userId);

        // then
        assertThat(goal.getUserId()).isEqualTo(userId);
        assertThat(goal.getAttendOn()).isEqualTo(LocalTime.of(20, 0));
        assertThat(goal.getRest10MinSec()).isEqualTo(120L);
        assertThat(goal.getMealNum()).isEqualTo(3);
        assertThat(goal.getMeal1TimeSec()).isEqualTo(3_600L);
    }

    @Test
    void 기본_목표는_회원_식별자가_없으면_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> Goal.createDefault(null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 음수_목표값이면_목표_생성에_실패한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> Goal.create(
                        1L,
                        2L,
                        LocalTime.of(9, 30),
                        -1L,
                        2,
                        1_800L
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }
}