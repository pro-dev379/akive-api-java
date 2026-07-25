package net.akive.api.auth.domain.login;

import net.akive.api.common.exception.business.BadRequestException;

import net.akive.api.common.domain.Email;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.BLANK_VALUE;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_EMAIL_FORMAT;
import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.exception.business.BadRequestError.VALUE_NOT_POSITIVE;
import static net.akive.api.user.domain.RoleType.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LoginUserTest {

    @Test
    void 로그인_회원을_생성한다() {
        // when
        LoginUser loginUser = LoginUser.create(1L, new Email("test@test.com"), "encoded", USER);

        // then
        assertThat(loginUser.userId()).isEqualTo(1L);
        assertThat(loginUser.email()).isEqualTo(new Email("test@test.com"));
        assertThat(loginUser.passwordValue()).isEqualTo("encoded");
        assertThat(loginUser.roleType()).isEqualTo(USER);
        assertThat(loginUser.hasPassword()).isTrue();
    }

    @Test
    void 회원_식별자가_없으면_로그인_회원_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> LoginUser.create(null, new Email("test@test.com"), "encoded", USER))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 회원_식별자가_양수가_아니면_로그인_회원_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> LoginUser.create(0L, new Email("test@test.com"), "encoded", USER))
                .extracting(BadRequestException::getError)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 이메일_형식이_올바르지_않으면_로그인_회원_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> LoginUser.create(1L, new Email("invalid-email"), "encoded", USER))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_EMAIL_FORMAT);
    }

    @Test
    void 회원_패스워드가_공백이면_로그인_회원_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> LoginUser.create(1L, new Email("test@test.com"), "   ", USER))
                .extracting(BadRequestException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 회원_역할이_없으면_로그인_회원_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> LoginUser.create(1L, new Email("test@test.com"), "encoded", null))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 레거시_패스워드를_사용하는_회원은_신규_패스워드_값이_null일_수_있다() {
        // when
        LoginUser loginUser = LoginUser.create(1L, new Email("test@test.com"), null, USER);

        // then
        assertThat(loginUser.password()).isNull();
        assertThat(loginUser.hasPassword()).isFalse();
    }
}
