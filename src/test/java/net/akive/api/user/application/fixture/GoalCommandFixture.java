package net.akive.api.user.application.fixture;

import net.akive.api.user.application.provided.dto.GoalCreateCommand;

import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class GoalCommandFixture {

    private GoalCommandFixture() {
    }

    public static GoalCreateCommand createGoalCreateCommand() {
        return createGoalCreateCommand(USER_ID);
    }

    public static GoalCreateCommand createGoalCreateCommand(Long userId) {
        return new GoalCreateCommand(
                userId,
                DEFAULT_GOAL_ATTEND_ON,
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        );
    }
}
