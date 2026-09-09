package net.akive.api.user.domain;

import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.GoalFixture.*;
import static net.akive.api.user.domain.UserFixture.createUser;
import static net.akive.api.user.domain.UserPolicy.*;
import static org.assertj.core.api.Assertions.assertThat;

class GoalTest {

    @Test
    void 저장된_목표를_복원한다() {
        User user = createUser();

        Goal goal = Goal.reconstruct(
                GOAL_ID,
                user,
                DEFAULT_GOAL_ATTEND_ON,
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        );

        assertThat(goal.getGoalId()).isEqualTo(GOAL_ID);
        assertThat(goal.getUser()).isEqualTo(user);
        assertThat(goal.getAttendOn()).isEqualTo(DEFAULT_GOAL_ATTEND_ON);
        assertThat(goal.getRest10MinSec()).isEqualTo(DEFAULT_GOAL_REST_10_MIN_SEC);
        assertThat(goal.getMealNum()).isEqualTo(DEFAULT_GOAL_MEAL_NUM);
        assertThat(goal.getMeal1TimeSec()).isEqualTo(DEFAULT_GOAL_MEAL_1_TIME_SEC);
    }

    @Test
    void 기본_목표를_생성한다() {
        User user = createUser();

        Goal goal = Goal.createDefault(user);

        assertThat(goal.getUser()).isEqualTo(user);
        assertThat(goal.getAttendOn()).isEqualTo(DEFAULT_GOAL_ATTEND_ON);
        assertThat(goal.getRest10MinSec()).isEqualTo(DEFAULT_GOAL_REST_10_MIN_SEC);
        assertThat(goal.getMealNum()).isEqualTo(DEFAULT_GOAL_MEAL_NUM);
        assertThat(goal.getMeal1TimeSec()).isEqualTo(DEFAULT_GOAL_MEAL_1_TIME_SEC);
    }


    @Test
    void 같은_식별자를_가진_목표는_같게_취급한다() {
        Goal goal = createReconstructedGoal(GOAL_ID);
        Goal sameGoal = createReconstructedGoal(GOAL_ID);

        assertThat(goal).isEqualTo(sameGoal);
        assertThat(goal.hashCode()).isEqualTo(sameGoal.hashCode());
    }

    @Test
    void 식별자가_다른_목표는_다르게_취급한다() {
        Goal goal = createReconstructedGoal(GOAL_ID);
        Goal sameGoal = createReconstructedGoal(ANOTHER_GOAL_ID);

        assertThat(goal).isNotEqualTo(sameGoal);
    }

    @Test
    void 식별자가_없는_목표끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = createUser();
        Goal goal = Goal.createDefault(user);
        Goal anotherGoal = Goal.createDefault(user);

        assertThat(goal).isNotEqualTo(anotherGoal);
    }
}