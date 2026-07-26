package net.akive.api.user.infrastructure.adapter;

import net.akive.api.common.domain.Email;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.Goal;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class GoalJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원의_기본_목표를_저장한다() {
        // given
        User savedUser = saveUser();
        Goal goal = Goal.createDefault(savedUser.getUserId());

        // when
        Goal savedGoal = saveGoalPort.save(goal);

        // then
        assertThat(savedGoal.getGoalId()).isNotNull();
    }

    private User saveUser() {
        User user = User.signUpByEmail(
                new Email("test@example.com"),
                "nickname",
                "encodedPassword",
                USER
        );

        return saveUserPort.save(user);
    }
}
