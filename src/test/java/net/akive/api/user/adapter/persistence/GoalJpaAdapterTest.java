package net.akive.api.user.adapter.persistence;

import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.UserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;

class GoalJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원의_기본_목표를_저장하고_식별자로_조회한다() {
        User savedUser = userRepository.save(createUser());
        Goal goal = Goal.createDefault(savedUser);

        Goal savedGoal = goalRepository.save(goal);
        flushAndClear();

        Goal foundGoal = goalRepository.findByGoalId(savedGoal.getGoalId()).orElseThrow();

        assertThat(foundGoal.getGoalId()).isEqualTo(savedGoal.getGoalId());
        assertThat(foundGoal.getUser()).isEqualTo(savedUser);
        assertThat(foundGoal.getAttendOn()).isEqualTo(DEFAULT_GOAL_ATTEND_ON);
        assertThat(foundGoal.getRest10MinSec()).isEqualTo(DEFAULT_GOAL_REST_10_MIN_SEC);
        assertThat(foundGoal.getMealNum()).isEqualTo(DEFAULT_GOAL_MEAL_NUM);
        assertThat(foundGoal.getMeal1TimeSec()).isEqualTo(DEFAULT_GOAL_MEAL_1_TIME_SEC);
    }

    @Test
    void 회원의_식별자로_가장_최신_목표_한_개를_조회한다() {
        User savedUser = userRepository.save(createUser());
        goalRepository.save(Goal.createDefault(savedUser));
        Goal secondGoal = goalRepository.save(Goal.createDefault(savedUser));
        flushAndClear();

        Goal foundGoal = goalRepository.findLatestByUserIdAndUserNotWithdrawn(savedUser.getUserId()).orElseThrow();

        assertThat(foundGoal).isEqualTo(secondGoal);
    }
}
