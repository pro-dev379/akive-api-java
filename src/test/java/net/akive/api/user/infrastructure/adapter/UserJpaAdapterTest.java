package net.akive.api.user.infrastructure.adapter;

import net.akive.api.common.domain.Email;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class UserJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 이메일로_회원가입_후_회원을_저장한다() {
        // given
        User user = signUpByEmail();

        // when
        User savedUser = saveUserPort.save(user);

        // then
        assertThat(savedUser.getUserId()).isNotNull();
    }

    @Test
    void 이미_가입된_이메일로는_중복_검증을_통과할_수_없다() {
        // given
        User user = signUpByEmail();
        User savedUser = saveUserPort.save(user);

        // when
        boolean exists = validateUserPort.existsByEmail(savedUser.getEmail());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 아직_가입되지_않은_이메일은_중복_검증을_통과한다() {
        // when
        boolean exists = validateUserPort.existsByEmail(new Email("none@example.com"));

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 이메일을_통해_이메일로_가입된_회원을_활성_상태로_조회할_수_있다() {
        // given
        User user = signUpByEmail();
        User savedUser = saveUserPort.save(user);

        // when
        boolean exists = validateUserPort.existsByEmailAndLeaveFalse(savedUser.getEmail());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 가입되지_않은_이메일로는_활성_회원을_조회할_수_없다() {
        // when
        boolean exists = validateUserPort.existsByEmailAndLeaveFalse(new Email("none@example.com"));

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 식별자를_통해_이메일로_가입된_회원을_활성_상태로_조회할_수_있다() {
        // given
        User user = signUpByEmail();
        User savedUser = saveUserPort.save(user);

        // when
        boolean exists = validateUserPort.existsByUserIdAndLeaveFalse(savedUser.getUserId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 존재하지_않는_회원_식별자로는_활성_회원을_조회할_수_없다() {
        // when
        boolean exists = validateUserPort.existsByUserIdAndLeaveFalse(999L);

        // then
        assertThat(exists).isFalse();
    }

    private User signUpByEmail() {
        return User.signUpByEmail(
                new Email("test@example.com"),
                "nickname",
                "encodedPassword",
                USER
        );
    }
}
