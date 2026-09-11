package net.akive.api.user.adapter.webapi;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.application.provided.GoalFinder;
import net.akive.api.user.domain.Goal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import static net.akive.api.common.exception.business.ResourceNotFoundError.GOAL_NOT_FOUND;
import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@WebMvcTest(MemberGoalApi.class)
class MemberGoalApiTest extends PrivateApiTestSupport {

    private static final String MEMBER_LATEST_GOAL_URL = "/members/me/goals/latest";

    @MockitoBean
    GoalFinder goalFinder;

    @Test
    void 최신_목표를_조회한다() {
        Goal goal = Goal.createDefault(createUser(USER_ID));
        given(goalFinder.findLatestByUserIdAndUserNotWithdrawn(USER_ID)).willReturn(goal);

        MvcTestResultAssert result = get(MEMBER_LATEST_GOAL_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.attendOn").isEqualTo(DEFAULT_GOAL_ATTEND_ON.toString());
        result.bodyJson().extractingPath("$.rest10MinSec").isEqualTo((int) DEFAULT_GOAL_REST_10_MIN_SEC);
        result.bodyJson().extractingPath("$.mealNum").isEqualTo(DEFAULT_GOAL_MEAL_NUM);
        result.bodyJson().extractingPath("$.meal1TimeSec").isEqualTo((int) DEFAULT_GOAL_MEAL_1_TIME_SEC);
        then(goalFinder).should().findLatestByUserIdAndUserNotWithdrawn(USER_ID);
    }

    @Test
    void 최신_목표를_조회할_수_없으면_404를_응답한다() {
        given(goalFinder.findLatestByUserIdAndUserNotWithdrawn(UNKNOWN_USER_ID))
                .willThrow(new ResourceNotFoundException(GOAL_NOT_FOUND));

        MvcTestResultAssert result = get(MEMBER_LATEST_GOAL_URL, UNKNOWN_USER_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, GOAL_NOT_FOUND);
        then(goalFinder).should().findLatestByUserIdAndUserNotWithdrawn(UNKNOWN_USER_ID);
    }
}
