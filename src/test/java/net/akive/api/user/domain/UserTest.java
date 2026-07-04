package net.akive.api.user.domain;

import org.junit.jupiter.api.Test;

import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.LoginType.SNS;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void 회원을_생성한다() {
        // given
        Long userId = 1L;

        // when
        User user = User.create(
                userId,
                "test@test.com",
                USER,
                SNS,
                "akive",
                "encoded",
                "https://example.com/image.png",
                "목표",
                true,
                "탈퇴 사유"
        );

        // then
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo("test@test.com");
        assertThat(user.getRoleType()).isEqualTo(USER);
        assertThat(user.getLoginType()).isEqualTo(SNS);
        assertThat(user.getNickname()).isEqualTo("akive");
        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.getImgUrl()).isEqualTo("https://example.com/image.png");
        assertThat(user.getAim()).isEqualTo("목표");
        assertThat(user.isLeave()).isTrue();
        assertThat(user.getLeaveReason()).isEqualTo("탈퇴 사유");
    }

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

    @Test
    void 비밀번호를_변경한다() {
        // given
        User user = User.signUpByEmail(
                "test@test.com",
                "akive",
                "encoded",
                USER
        );

        // when
        user.updatePassword("new-encoded");

        // then
        assertThat(user.getPassword()).isEqualTo("new-encoded");
    }
}