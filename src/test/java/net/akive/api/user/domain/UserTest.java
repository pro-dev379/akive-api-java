package net.akive.api.user.domain;

import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void 이메일_회원가입_시_기본값으로_생성된다() {
        User user = User.signUpByEmail(
                "test@test.com",
                "akive",
                "encoded",
                USER
        );

        assertThat(user.getLoginType()).isEqualTo(EMAIL);
        assertThat(user.isLeave()).isFalse();
    }
}