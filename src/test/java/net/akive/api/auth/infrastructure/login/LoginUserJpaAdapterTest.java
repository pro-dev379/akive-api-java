package net.akive.api.auth.infrastructure.login;

import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.login.exception.LoginException;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;

import static net.akive.api.auth.domain.login.exception.LoginErrorCode.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LoginUserJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 이메일_로그인_회원이_존재하면_조회할_수_있다() {
        // given
        User user = User.signUpByEmail(
                "test@test.com",
                "USER@abcde",
                "encoded-password",
                USER
        );

        User savedUser = saveUserPort.save(user);

        // when
        LoginUser loginUser =
                loadEmailLoginUserPort.loadByEmailAndLeaveFalse(savedUser.getEmail());

        // then
        assertThat(loginUser.userId()).isEqualTo(savedUser.getUserId());
        assertThat(loginUser.email()).isEqualTo(savedUser.getEmail());
        assertThat(loginUser.password()).isEqualTo(savedUser.getPassword());
        assertThat(loginUser.roleType()).isEqualTo(savedUser.getRoleType());
    }

    @Test
    void 이메일_로그인_회원이_존재하지_않으면_로그인할_수_없다() {
        // given
        String email = "none@test.com";

        // when & then
        assertThatExceptionOfType(LoginException.class)
                .isThrownBy(() -> loadEmailLoginUserPort.loadByEmailAndLeaveFalse(email))
                .extracting(LoginException::getErrorCode)
                .isEqualTo(INVALID_EMAIL_OR_PASSWORD);
    }
}
