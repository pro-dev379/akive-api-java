package net.akive.api.user.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class GoalTest {

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