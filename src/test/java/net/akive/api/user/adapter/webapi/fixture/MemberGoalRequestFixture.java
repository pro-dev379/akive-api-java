package net.akive.api.user.adapter.webapi.fixture;

import net.akive.api.user.adapter.webapi.dto.request.GoalCreateRequest;

import java.util.stream.Stream;

import static net.akive.api.user.domain.UserPolicy.*;

public class MemberGoalRequestFixture {

    public static GoalCreateRequest createGoalCreateRequest() {
        return new GoalCreateRequest(
                DEFAULT_GOAL_ATTEND_ON,
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        );
    }

    public static Stream<GoalCreateRequest> invalidGoalCreateRequests() {
        return Stream.of(
                new GoalCreateRequest(
                        null,
                        DEFAULT_GOAL_REST_10_MIN_SEC,
                        DEFAULT_GOAL_MEAL_NUM,
                        DEFAULT_GOAL_MEAL_1_TIME_SEC
                ),
                new GoalCreateRequest(
                        DEFAULT_GOAL_ATTEND_ON,
                        -1,
                        DEFAULT_GOAL_MEAL_NUM,
                        DEFAULT_GOAL_MEAL_1_TIME_SEC
                ),
                new GoalCreateRequest(
                        DEFAULT_GOAL_ATTEND_ON,
                        DEFAULT_GOAL_REST_10_MIN_SEC,
                        -1,
                        DEFAULT_GOAL_MEAL_1_TIME_SEC
                ),
                new GoalCreateRequest(
                        DEFAULT_GOAL_ATTEND_ON,
                        DEFAULT_GOAL_REST_10_MIN_SEC,
                        DEFAULT_GOAL_MEAL_NUM,
                        -1
                )
        );
    }
}
