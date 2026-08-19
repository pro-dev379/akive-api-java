package net.akive.api.user.adapter.persistence;

import jakarta.persistence.EntityManager;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.adapter.persistence.entity.GoalEntity;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static net.akive.api.user.domain.UserFixture.createUser;
import static net.akive.api.user.domain.UserPolicy.*;
import static org.assertj.core.api.Assertions.assertThat;

class GoalJpaAdapterTest extends RepositoryTestSupport {

    @Autowired
    GoalJpaRepository goalJpaRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void 회원의_기본_목표를_저장한다() {
        User savedUser = userRepository.save(createUser());
        Goal goal = Goal.createDefault(savedUser);

        Goal savedGoal = goalRepository.save(goal);

        entityManager.flush();
        entityManager.clear();

        GoalEntity foundGoalEntity = goalJpaRepository.findById(savedGoal.getGoalId()).orElseThrow();
        Goal foundGoal = foundGoalEntity.to();

        assertThat(foundGoal.getGoalId()).isEqualTo(savedGoal.getGoalId());
        assertThat(foundGoal.getUser()).isEqualTo(savedUser);
        assertThat(foundGoal.getAttendOn()).isEqualTo(DEFAULT_GOAL_ATTEND_ON);
        assertThat(foundGoal.getRest10MinSec()).isEqualTo(DEFAULT_GOAL_REST_10_MIN_SEC);
        assertThat(foundGoal.getMealNum()).isEqualTo(DEFAULT_GOAL_MEAL_NUM);
        assertThat(foundGoal.getMeal1TimeSec()).isEqualTo(DEFAULT_GOAL_MEAL_1_TIME_SEC);
    }
}
