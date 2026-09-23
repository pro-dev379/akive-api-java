package net.akive.api.user.domain.fixture;

import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.GoalVersion;

import java.time.LocalDateTime;

import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;

public class GoalFixture {

    private GoalFixture() {
    }

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

    public static GoalVersion goalVersionWithoutMealAndRestAllowance(LocalDateTime createdAt) {
        return createGoalVersion(0, 0, 0, createdAt);
    }

    public static GoalVersion createGoalVersion(LocalDateTime createdAt) {
        return createGoalVersion(
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC,
                createdAt
        );
    }

    public static GoalVersion createGoalVersion(
            long rest10MinSec,
            int mealNum,
            long meal1TimeSec,
            LocalDateTime createdAt
    ) {
        return new GoalVersion(rest10MinSec, mealNum, meal1TimeSec, createdAt);
    }
}
