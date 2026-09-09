package net.akive.api.user.domain.fixture;

import net.akive.api.user.domain.Goal;

import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;

public class GoalFixture {

    public static Long GOAL_ID = 1L;
    public static Long ANOTHER_GOAL_ID = 2L;

    public static Goal createReconstructedGoal(Long goalId) {
        return Goal.reconstruct(
                goalId,
                createUser(),
                DEFAULT_GOAL_ATTEND_ON,
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        );
    }
}
