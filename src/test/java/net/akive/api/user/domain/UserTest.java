package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.BLANK_VALUE;
import static net.akive.api.user.domain.RoleType.MEMBER;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserDetailFixture.PROFILE_IMAGE_ASSET_ID;
import static net.akive.api.user.domain.fixture.UserDetailFixture.createUserDetail;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWAL_REASON;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWN_AT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

    @Test
    void 저장된_회원_정보를_복원한다() {
        UserDetail userDetail = createUserDetail();
        Withdrawal withdrawal = Withdrawal.none();

        User user = User.reconstruct(
                USER_ID,
                EMAIL,
                ROLE_TYPE,
                LOGIN_TYPE,
                PASSWORD_HASH,
                userDetail,
                withdrawal,
                CREATED_AT
        );

        assertThat(user.getUserId()).isEqualTo(USER_ID);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getRoleType()).isEqualTo(MEMBER);
        assertThat(user.getLoginType()).isEqualTo(LOGIN_TYPE);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getUserDetail()).isEqualTo(userDetail);
        assertThat(user.getWithdrawal()).isEqualTo(withdrawal);
        assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void 이메일로_가입한_회원을_생성한다() {
        User user = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getRoleType()).isEqualTo(ROLE_TYPE);
        assertThat(user.getLoginType()).isEqualTo(LOGIN_TYPE);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getUserDetail().nickname()).isEqualTo(NICKNAME);
        assertThat(user.getUserDetail().profileImageAssetId()).isNull();
        assertThat(user.getUserDetail().aim()).isNull();
        assertThat(user.getWithdrawal().withdrawnAt()).isNull();
        assertThat(user.getWithdrawal().withdrawalReason()).isNull();
    }

    @Test
    void 패스워드가_있는지_확인한다() {
        User user = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);

        assertThat(user.hasPassword()).isTrue();

        User anotherUser = User.reconstruct(
                USER_ID,
                EMAIL,
                ROLE_TYPE,
                LOGIN_TYPE,
                null,
                createUserDetail(),
                Withdrawal.none(),
                CREATED_AT
        );

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

        user.updateUserDetail("  akive  ", PROFILE_IMAGE_ASSET_ID, "목표");

        assertThat(user.getUserDetail().nickname()).isEqualTo("akive");
        assertThat(user.getUserDetail().profileImageAssetId()).isEqualTo(PROFILE_IMAGE_ASSET_ID);
        assertThat(user.getUserDetail().aim()).isEqualTo("목표");
    }

    @Test
    void 회원_탈퇴를_완료한다() {
        User user = createUser();

        user.withdraw(WITHDRAWN_AT, WITHDRAWAL_REASON);

        assertThat(user.getWithdrawal().withdrawnAt()).isEqualTo(WITHDRAWN_AT);
        assertThat(user.getWithdrawal().withdrawalReason()).isEqualTo(WITHDRAWAL_REASON);
    }

    @Test
    void 회원_탈퇴_여부를_반환한다() {
        User user = createUser();

        assertThat(user.isWithdrawn()).isFalse();

        user.withdraw(WITHDRAWN_AT, WITHDRAWAL_REASON);

        assertThat(user.isWithdrawn()).isTrue();
    }

    @Test
    void 같은_식별자를_가진_회원은_같게_취급한다() {
        User user = createReconstructedUser(USER_ID);
        User sameUser = createReconstructedUser(USER_ID);

        assertThat(user).isEqualTo(sameUser);
        assertThat(user.hashCode()).isEqualTo(sameUser.hashCode());
    }

    @Test
    void 식별자가_다른_회원은_다르게_취급한다() {
        User user = createReconstructedUser(USER_ID);
        User anotherUser = createReconstructedUser(ANOTHER_USER_ID);

        assertThat(user).isNotEqualTo(anotherUser);
    }

    @Test
    void 식별자가_없는_회원끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        User user = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);
        User anotherUser = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);

        assertThat(user).isNotEqualTo(anotherUser);
    }
}
