package net.akive.api.user.infrastructure;

import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class GoalJpaRepositoryAdapterTest {

    @Autowired
    GoalJpaRepositoryAdapter goalJpaRepositoryAdapter;

    @Autowired
    UserJpaRepositoryAdapter userJpaRepositoryAdapter;

    @Test
    void 회원의_목표를_저장한다() {
        // given
        User savedUser = saveUser();
        Goal goal = Goal.createDefault(savedUser.getUserId());

        // when
        Goal savedGoal = goalJpaRepositoryAdapter.save(goal);

        // then
        assertThat(savedGoal.getGoalId()).isNotNull();
    }

    private User saveUser() {
        User user = User.signUpByEmail(
                "test@example.com",
                "nickname",
                "encodedPassword",
                USER
        );

        return userJpaRepositoryAdapter.save(user);
    }
}