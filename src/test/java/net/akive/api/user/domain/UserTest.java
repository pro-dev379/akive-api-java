package net.akive.api.user.domain;

import net.akive.api.common.domain.Email;
import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.BLANK_VALUE;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.user.domain.LoginType.EMAIL;
import static net.akive.api.user.domain.LoginType.SNS;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

    private static final Email VALID_EMAIL = new Email("test@test.com");
    private static final String VALID_NICKNAME = "akive";
    private static final String VALID_ENCODED_PASSWORD = "encoded-password";

    @Test
    void 회원을_생성한다() {
        // given
        Long userId = 1L;

        // when
        User user = User.create(
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
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> User.signUpByEmail(
                        VALID_EMAIL,
                        "   ",
                        VALID_ENCODED_PASSWORD,
                        USER
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 이메일_회원가입_시_닉네임이_최대_길이를_초과하지_않으면_회원이_정상_생성된다() {
        String nickname = "a".repeat(100);

        User user = User.signUpByEmail(
                VALID_EMAIL,
                nickname,
                VALID_ENCODED_PASSWORD,
                USER
        );

        assertThat(user.getNickname()).isEqualTo(nickname);
    }

    @Test
    void 이메일_회원가입_시_닉네임이_최대_길이를_초과하면_회원_생성에_실패한다() {
        String nickname = "a".repeat(101);

        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> User.signUpByEmail(
                        VALID_EMAIL,
                        nickname,
                        VALID_ENCODED_PASSWORD,
                        USER
                ))
                .extracting(DomainValidationException::getErrorCode)
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
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> User.signUpByEmail(
                        VALID_EMAIL,
                        VALID_NICKNAME,
                        "   ",
                        USER
                ))
                .extracting(DomainValidationException::getErrorCode)
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
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> user.updatePassword("   "))
                .extracting(DomainValidationException::getErrorCode)
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
