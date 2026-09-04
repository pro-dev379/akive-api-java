package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.BLANK_VALUE;
import static net.akive.api.user.domain.RoleType.MEMBER;
import static net.akive.api.user.domain.UserFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

    @Test
    void 저장된_회원_정보를_복원한다() {
        Long userId = 1L;
        UserDetail userDetail = UserDetail.of(NICKNAME, "https://example.com", "aim");
        Leave leave = Leave.none();

        User user = User.reconstruct(userId, EMAIL, ROLE_TYPE, LOGIN_TYPE, PASSWORD_HASH, userDetail, leave);

        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getRoleType()).isEqualTo(MEMBER);
        assertThat(user.getLoginType()).isEqualTo(LOGIN_TYPE);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getUserDetail()).isEqualTo(userDetail);
        assertThat(user.getLeave()).isEqualTo(leave);
    }

    @Test
    void 이메일로_가입한_회원을_생성한다() {
        User user = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getRoleType()).isEqualTo(ROLE_TYPE);
        assertThat(user.getLoginType()).isEqualTo(LOGIN_TYPE);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getUserDetail().nickname()).isEqualTo(NICKNAME);
        assertThat(user.getUserDetail().imgUrl()).isNull();
        assertThat(user.getUserDetail().aim()).isNull();
        assertThat(user.getLeave().isLeave()).isFalse();
        assertThat(user.getLeave().reason()).isNull();
    }

    @Test
    void 패스워드가_있는지_확인한다() {
        User user = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);

        assertThat(user.hasPassword()).isTrue();

        Long userId = 1L;
        UserDetail userDetail = UserDetail.of(NICKNAME, "https://example.com", "aim");
        Leave leave = Leave.none();
        String passwordHash = null;

        User anotherUser = User.reconstruct(userId, EMAIL, ROLE_TYPE, LOGIN_TYPE, passwordHash, userDetail, leave);

        assertThat(anotherUser.hasPassword()).isFalse();
    }

    @Test
    void 패스워드_해시를_변경한다() {
        User user = createUser();

        user.updatePasswordHash("  new-password-hash  ");

        assertThat(user.getPasswordHash()).isEqualTo("new-password-hash");
    }

    @Test
    void 공백_패스워드_해시로는_변경할_수_없다() {
        User user = createUser();

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> user.updatePasswordHash("   "))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 회원_상세_정보를_변경한다() {
        User user = createUser();

        user.updateUserDetail("  akive  ", "https://example.com/image.png", "목표");

        assertThat(user.getUserDetail().nickname()).isEqualTo("akive");
        assertThat(user.getUserDetail().imgUrl()).isEqualTo("https://example.com/image.png");
        assertThat(user.getUserDetail().aim()).isEqualTo("목표");
    }

    @Test
    void 탈퇴를_완료한다() {
        User user = createUser();

        user.completeLeave("탈퇴 사유");

        assertThat(user.getLeave().isLeave()).isTrue();
        assertThat(user.getLeave().reason()).isEqualTo("탈퇴 사유");
    }

    @Test
    void 같은_식별자를_가진_회원은_같게_취급한다() {
        User user = createPersistedUser(1L);
        User sameUser = createPersistedUser(1L);

        assertThat(user).isEqualTo(sameUser);
        assertThat(user.hashCode()).isEqualTo(sameUser.hashCode());
    }

    @Test
    void 식별자가_다른_회원은_다르게_취급한다() {
        User user = createPersistedUser(1L);
        User anotherUser = createPersistedUser(2L);

        assertThat(user).isNotEqualTo(anotherUser);
    }

    @Test
    void 식별자가_없는_회원끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);
        User anotherUser = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);

        assertThat(user).isNotEqualTo(anotherUser);
    }
}
