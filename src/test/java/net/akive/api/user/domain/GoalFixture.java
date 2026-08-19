package net.akive.api.user.domain;

import static net.akive.api.user.domain.UserFixture.createUser;
import static net.akive.api.user.domain.UserPolicy.*;

public class GoalFixture {

    public static Goal createPersistedGoal(Long goalId) {
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
