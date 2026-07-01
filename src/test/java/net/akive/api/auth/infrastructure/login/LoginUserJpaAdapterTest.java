package net.akive.api.auth.infrastructure.login;

import net.akive.api.auth.domain.login.LoginUser;
import net.akive.api.auth.domain.login.exception.LoginException;
import net.akive.api.user.application.port.out.SaveUserPort;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static net.akive.api.auth.domain.login.exception.LoginErrorCode.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class LoginUserJpaAdapterTest {

    @Autowired
    LoginUserJpaAdapter loginUserJpaAdapter;

    @Autowired
    SaveUserPort saveUserPort;

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
                loginUserJpaAdapter.findByEmailAndLeaveFalse(savedUser.getEmail());

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
        assertThatThrownBy(() -> loginUserJpaAdapter.findByEmailAndLeaveFalse(email))
                .isInstanceOf(LoginException.class)
                .hasMessage(INVALID_EMAIL_OR_PASSWORD.getMessage());
    }
}