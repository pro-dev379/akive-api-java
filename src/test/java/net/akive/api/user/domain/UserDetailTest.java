package net.akive.api.user.domain;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.*;
import static net.akive.api.user.domain.UserPolicy.AIM_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserDetailTest {

    @Test
    void 초기_회원_상세_정보를_생성한다() {
        UserDetail userDetail = UserDetail.initial(NICKNAME);

        assertThat(userDetail.nickname()).isEqualTo(NICKNAME);
        assertThat(userDetail.profileImageAssetId()).isNull();
        assertThat(userDetail.aim()).isNull();
    }

    @Test
    void 회원_상세_정보를_생성한다() {
        UserDetail userDetail = new UserDetail(NICKNAME, PROFILE_IMAGE_ASSET_ID, AIM);

        assertThat(userDetail.nickname()).isEqualTo(NICKNAME);
        assertThat(userDetail.profileImageAssetId()).isEqualTo(PROFILE_IMAGE_ASSET_ID);
        assertThat(userDetail.aim()).isEqualTo(AIM);
    }

    @Test
    void 닉네임_앞뒤에_공백이_있으면_제거된다() {
        UserDetail userDetail = new UserDetail("  akive  ", PROFILE_IMAGE_ASSET_ID, AIM);

        assertThat(userDetail.nickname()).isEqualTo("akive");
    }

    @Test
    void 닉네임은_최대_글자_수일_수_있다() {
        String nickname = "a".repeat(NICKNAME_MAX_LENGTH);

        UserDetail userDetail = new UserDetail(nickname, PROFILE_IMAGE_ASSET_ID, AIM);

        assertThat(userDetail.nickname()).isEqualTo(nickname);
    }

    @Test
    void 닉네임이_최대_글자_수를_초과하면_생성에_실패한다() {
        String nickname = "a".repeat(NICKNAME_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new UserDetail(nickname, PROFILE_IMAGE_ASSET_ID, AIM))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 닉네임이_없으면_생성에_실패한다() {
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new UserDetail(null, PROFILE_IMAGE_ASSET_ID, AIM))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new UserDetail("   ", PROFILE_IMAGE_ASSET_ID, AIM))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 목표는_최대_글자_수일_수_있다() {
        String aim = "a".repeat(AIM_MAX_LENGTH);

        UserDetail userDetail = new UserDetail(NICKNAME, PROFILE_IMAGE_ASSET_ID, aim);

        assertThat(userDetail.aim()).isEqualTo(aim);
    }

    @Test
    void 목표가_최대_글자_수를_초과하면_생성에_실패한다() {
        String aim = "a".repeat(AIM_MAX_LENGTH + 1);

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> new UserDetail(NICKNAME, PROFILE_IMAGE_ASSET_ID, aim))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
