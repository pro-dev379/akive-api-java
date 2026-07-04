package net.akive.api.user.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class GoalTest {

    @Test
    void 목표를_생성한다() {
        // given
        Long goalId = 1L;
        Long userId = 2L;
        LocalTime attendOn = LocalTime.of(9, 30);
        Integer rest10MinSec = 180;
        Integer mealNum = 2;
        Integer meal1TimeSec = 1_800;

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
        assertThat(goal.getRest10MinSec()).isEqualTo(120);
        assertThat(goal.getMealNum()).isEqualTo(3);
        assertThat(goal.getMeal1TimeSec()).isEqualTo(3_600);
    }
}