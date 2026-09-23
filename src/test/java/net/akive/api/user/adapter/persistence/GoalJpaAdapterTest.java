package net.akive.api.user.adapter.persistence;

import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.GoalVersion;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void 조회_시작일까지_생성된_최신_목표와_그_이후_조회_종료일까지_생성된_목표를_조회한다() {
        User savedUser = saveUser();
        LocalDate startOn = LocalDate.of(2026, 9, 14);
        LocalDate endOn = LocalDate.of(2026, 9, 20);
        saveGoalAt(savedUser, 100, LocalDateTime.of(2026, 9, 10, 0, 0));
        saveGoalAt(savedUser, 200, LocalDateTime.of(2026, 9, 13, 20, 0));
        saveGoalAt(savedUser, 210, LocalDateTime.of(2026, 9, 13, 20, 0));
        saveGoalAt(savedUser, 300, LocalDateTime.of(2026, 9, 16, 20, 0));
        saveGoalAt(savedUser, 400, LocalDateTime.of(2026, 9, 20, 16, 0));

        List<GoalVersion> goalVersions = goalRepository.findVersionsForPeriod(
                savedUser.getUserId(),
                startOn,
                endOn
        );

        assertThat(goalVersions).hasSize(2);
        assertThat(goalVersions).extracting(GoalVersion::rest10MinSec).containsExactly(210L, 300L);
        assertThat(goalVersions).allSatisfy(version -> assertThat(version.createdAt()).isNotNull());

        List<GoalVersion> singleDayVersions = goalRepository.findVersionsForPeriod(
                savedUser.getUserId(),
                startOn,
                startOn
        );
        assertThat(singleDayVersions).extracting(GoalVersion::rest10MinSec).containsExactly(210L);
    }

    private void saveGoalAt(User user, long rest10MinSec, LocalDateTime createdAt) {
        Goal goal = goalRepository.save(Goal.create(
                user,
                DEFAULT_GOAL_ATTEND_ON,
                rest10MinSec,
                DEFAULT_GOAL_MEAL_NUM,
                DEFAULT_GOAL_MEAL_1_TIME_SEC
        ));
        flushAndClear();
        entityManager.createNativeQuery("update goal set created_at = :createdAt where goal_id = :goalId")
                .setParameter("createdAt", createdAt)
                .setParameter("goalId", goal.getGoalId())
                .executeUpdate();
    }
}
