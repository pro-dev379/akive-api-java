package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BadRequestException;

import net.akive.api.common.domain.Email;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.BLANK_VALUE;
import static net.akive.api.common.exception.business.BadRequestError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.LoginType.SNS;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

    private static final Email VALID_EMAIL = new Email("test@test.com");
    private static final String VALID_NICKNAME = "akive";
    private static final String VALID_ENCODED_PASSWORD = "encoded-password";
    private static final String VALID_AIM = "목표";

    @Test
    void 회원을_생성한다() {
        // given
        Long userId = 1L;

        // when
        User user = User.reconstruct(
                userId,
                VALID_EMAIL,
                USER,
                SNS,
                VALID_NICKNAME,
                VALID_ENCODED_PASSWORD,
                "https://example.com/image.png",
                "목표",
                true,
                "탈퇴 사유"
        );

        // then
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(user.getRoleType()).isEqualTo(USER);
        assertThat(user.getLoginType()).isEqualTo(SNS);
        assertThat(user.getNickname()).isEqualTo(VALID_NICKNAME);
        assertThat(user.getPassword()).isEqualTo(VALID_ENCODED_PASSWORD);
        assertThat(user.getImgUrl()).isEqualTo("https://example.com/image.png");
        assertThat(user.getAim()).isEqualTo("목표");
        assertThat(user.isLeave()).isTrue();
        assertThat(user.getLeaveReason()).isEqualTo("탈퇴 사유");
    }

    @Test
    void 회원_생성_시_닉네임의_앞뒤_공백을_제거한다() {
        // when
        User user = User.reconstruct(
                1L,
                VALID_EMAIL,
                USER,
                SNS,
                "  akive  ",
                VALID_ENCODED_PASSWORD,
                null,
                null,
                false,
                null
        );

        // then
        assertThat(user.getNickname()).isEqualTo(VALID_NICKNAME);
    }

    @Test
    void 회원_생성_시_닉네임이_공백이면_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> User.reconstruct(
                        1L,
                        VALID_EMAIL,
                        USER,
                        SNS,
                        "   ",
                        VALID_ENCODED_PASSWORD,
                        null,
                        VALID_AIM,
                        false,
                        null
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 회원_생성_시_닉네임은_20자일_수_있다() {
        // given
        String nickname = "a".repeat(20);

        // when
        User user = User.reconstruct(
                1L,
                VALID_EMAIL,
                USER,
                SNS,
                nickname,
                VALID_ENCODED_PASSWORD,
                null,
                null,
                false,
                null
        );

        // then
        assertThat(user.getNickname()).isEqualTo(nickname);
    }

    @Test
    void 회원_생성_시_닉네임이_20자를_초과하면_생성에_실패한다() {
        // given
        String nickname = "a".repeat(21);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> User.reconstruct(
                        1L,
                        VALID_EMAIL,
                        USER,
                        SNS,
                        nickname,
                        VALID_ENCODED_PASSWORD,
                        null,
                        null,
                        false,
                        null
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 회원_생성_시_목표가_null이어도_생성한다() {
        // when
        User user = User.reconstruct(
                1L,
                VALID_EMAIL,
                USER,
                SNS,
                VALID_NICKNAME,
                VALID_ENCODED_PASSWORD,
                null,
                null,
                false,
                null
        );

        // then
        assertThat(user.getAim()).isNull();
    }

    @Test
    void 회원_생성_시_목표는_100자일_수_있다() {
        // given
        String aim = "a".repeat(100);

        // when
        User user = User.reconstruct(
                1L,
                VALID_EMAIL,
                USER,
                SNS,
                VALID_NICKNAME,
                VALID_ENCODED_PASSWORD,
                null,
                aim,
                false,
                null
        );

        // then
        assertThat(user.getAim()).isEqualTo(aim);
    }

    @Test
    void 회원_생성_시_목표가_100자를_초과하면_생성에_실패한다() {
        // given
        String aim = "a".repeat(101);

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> User.reconstruct(
                        1L,
                        VALID_EMAIL,
                        USER,
                        SNS,
                        VALID_NICKNAME,
                        VALID_ENCODED_PASSWORD,
                        null,
                        aim,
                        false,
                        null
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 이메일_회원가입_회원을_생성한다() {
        User user = User.signUpByEmail(
                VALID_EMAIL,
                VALID_NICKNAME,
                VALID_ENCODED_PASSWORD,
                USER
        );

        assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(user.getNickname()).isEqualTo(VALID_NICKNAME);
        assertThat(user.getPassword()).isEqualTo(VALID_ENCODED_PASSWORD);
        assertThat(user.getRoleType()).isEqualTo(USER);
        assertThat(user.getLoginType()).isEqualTo(EMAIL);
        assertThat(user.isLeave()).isFalse();
    }

    @Test
    void 이메일_회원가입_시_닉네임의_앞뒤_공백을_제거한다() {
        User user = User.signUpByEmail(
                VALID_EMAIL,
                "  akive  ",
                VALID_ENCODED_PASSWORD,
                USER
        );

        assertThat(user.getNickname()).isEqualTo(VALID_NICKNAME);
    }

    @Test
    void 이메일_회원가입_시_닉네임이_공백이면_회원_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> User.signUpByEmail(
                        VALID_EMAIL,
                        "   ",
                        VALID_ENCODED_PASSWORD,
                        USER
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 이메일_회원가입_시_닉네임은_20자일_수_있다() {
        String nickname = "a".repeat(20);

        User user = User.signUpByEmail(
                VALID_EMAIL,
                nickname,
                VALID_ENCODED_PASSWORD,
                USER
        );

        assertThat(user.getNickname()).isEqualTo(nickname);
    }

    @Test
    void 이메일_회원가입_시_닉네임이_20자를_초과하면_회원_생성에_실패한다() {
        String nickname = "a".repeat(21);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> User.signUpByEmail(
                        VALID_EMAIL,
                        nickname,
                        VALID_ENCODED_PASSWORD,
                        USER
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 이메일_회원가입_시_인코딩된_패스워드의_앞뒤_공백을_제거한다() {
        User user = User.signUpByEmail(
                VALID_EMAIL,
                VALID_NICKNAME,
                "  encoded-password  ",
                USER
        );

        assertThat(user.getPassword()).isEqualTo(VALID_ENCODED_PASSWORD);
    }

    @Test
    void 이메일_회원가입_시_인코딩된_패스워드가_공백이면_회원_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> User.signUpByEmail(
                        VALID_EMAIL,
                        VALID_NICKNAME,
                        "   ",
                        USER
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 패스워드를_변경한다() {
        // given
        User user = createEmailUser();

        // when
        user.updatePassword("new-encoded");

        // then
        assertThat(user.getPassword()).isEqualTo("new-encoded");
    }

    @Test
    void 패스워드_변경_시_앞뒤_공백을_제거한다() {
        // given
        User user = createEmailUser();

        // when
        user.updatePassword("  new-encoded  ");

        // then
        assertThat(user.getPassword()).isEqualTo("new-encoded");
    }

    @Test
    void 공백_패스워드로는_변경할_수_없다() {
        // given
        User user = createEmailUser();

        // when & then
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> user.updatePassword("   "))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    private User createEmailUser() {
        return User.signUpByEmail(
                VALID_EMAIL,
                VALID_NICKNAME,
                VALID_ENCODED_PASSWORD,
                USER
        );
    }
}
