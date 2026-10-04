package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.GOAL_ATTEND_ON_NOT_ALLOWED;
import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.GoalFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
    void 목표를_생성한다() {
        User user = createUser();

        Goal goal = Goal.create(
                user,
                DEFAULT_GOAL_ATTEND_ON,
                DEFAULT_GOAL_REST_10_MIN_SEC,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        );

        assertThat(goal.getGoalId()).isNull();
        assertThat(goal.getUser()).isEqualTo(user);
        assertThat(goal.getAttendOn()).isEqualTo(DEFAULT_GOAL_ATTEND_ON);
        assertThat(goal.getRest10MinSec()).isEqualTo(DEFAULT_GOAL_REST_10_MIN_SEC);
        assertThat(goal.getMealNum()).isEqualTo(DEFAULT_GOAL_MEAL_NUM);
        assertThat(goal.getMeal1TimeSec()).isEqualTo(DEFAULT_GOAL_MEAL_1_TIME_SEC);
    }

    @ParameterizedTest
    @MethodSource("allowedGoalAttendTimesAroundBoundary")
    void 오전_5시_이전과_5시_5분부터는_목표_출석_시간으로_설정할_수_있다(LocalTime attendOn) {
        assertThatCode(() -> createGoal(attendOn)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("blockedGoalAttendTimes")
    void 오전_5시부터_5시_5분_전까지는_목표_출석_시간으로_설정할_수_없다(LocalTime attendOn) {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> createGoal(attendOn))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(GOAL_ATTEND_ON_NOT_ALLOWED);
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

    private static Stream<LocalTime> allowedGoalAttendTimesAroundBoundary() {
        return Stream.of(
                LocalTime.of(4, 59, 59, 999_999_999),
                LocalTime.of(5, 5)
        );
    }

    private static Stream<LocalTime> blockedGoalAttendTimes() {
        return Stream.of(
                LocalTime.of(5, 0),
                LocalTime.of(5, 4),
                LocalTime.of(5, 4, 59, 999_999_999)
        );
    }
}
