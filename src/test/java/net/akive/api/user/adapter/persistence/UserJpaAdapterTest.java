package net.akive.api.user.adapter.persistence;

import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.Withdrawal;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원을_저장한다() {
        User user = User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);
        user.updateUserDetail(NICKNAME, PROFILE_IMAGE_ASSET_ID, AIM);

        User savedUser = userRepository.save(user);
        flushAndClear();

        User foundUser = userRepository.findByUserIdAndNotWithdrawn(savedUser.getUserId()).orElseThrow();

        assertThat(foundUser.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundUser.getEmail()).isEqualTo(EMAIL);
        assertThat(foundUser.getRoleType()).isEqualTo(ROLE_TYPE);
        assertThat(foundUser.getLoginType()).isEqualTo(LOGIN_TYPE);
        assertThat(foundUser.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(foundUser.getUserDetail().nickname()).isEqualTo(NICKNAME);
        assertThat(foundUser.getUserDetail().profileImageAssetId()).isEqualTo(PROFILE_IMAGE_ASSET_ID);
        assertThat(foundUser.getUserDetail().aim()).isEqualTo(AIM);
        assertThat(foundUser.getWithdrawal()).isEqualTo(Withdrawal.none());
        assertThat(foundUser.getCreatedAt()).isNotNull();
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회한다() {
        User user = createUser();
        User savedUser = userRepository.save(user);
        flushAndClear();

        Optional<User> found = userRepository.findByUserIdAndNotWithdrawn(savedUser.getUserId());

        assertThat(found).contains(savedUser);
    }

    @Test
    void 탈퇴한_회원은_회원_식별자로_조회되지_않는다() {
        User user = createWithdrawnUser();
        User savedUser = userRepository.save(user);
        flushAndClear();

        Optional<User> found = userRepository.findByUserIdAndNotWithdrawn(savedUser.getUserId());

        assertThat(found).isEmpty();
    }

    @Test
    void 이메일로_탈퇴하지_않은_회원을_조회한다() {
        User user = createUser(EMAIL);
        User savedUser = userRepository.save(user);
        flushAndClear();

        Optional<User> found = userRepository.findByEmailAndNotWithdrawn(EMAIL);

        assertThat(found).contains(savedUser);
    }

    @Test
    void 탈퇴한_회원은_이메일로_조회되지_않는다() {
        User user = createWithdrawnUser(WITHDRAWN_USER_EMAIL);
        userRepository.save(user);
        flushAndClear();

        Optional<User> foundUser = userRepository.findByEmailAndNotWithdrawn(WITHDRAWN_USER_EMAIL);

        assertThat(foundUser).isEmpty();
    }

    @Test
    void 이메일에_해당하는_회원이_존재하는지_확인한다() {
        User user = createUser(EMAIL);
        userRepository.save(user);
        flushAndClear();

        boolean exists = userRepository.existsByEmail(EMAIL);

        assertThat(exists).isTrue();
    }

    @Test
    void 이메일에_해당하는_회원이_없으면_존재하지_않는다고_응답한다() {
        boolean exists = userRepository.existsByEmail(createEmail("unknown@test.com"));

        assertThat(exists).isFalse();
    }

    @Test
    void 탈퇴한_회원도_이메일_존재_여부_확인_대상에_포함한다() {
        User user = createWithdrawnUser(WITHDRAWN_USER_EMAIL);
        userRepository.save(user);
        flushAndClear();

        boolean exists = userRepository.existsByEmail(WITHDRAWN_USER_EMAIL);

        assertThat(exists).isTrue();
    }

    @Test
    void 닉네임에_해당하는_회원이_존재하는지_확인한다() {
        User user = createUser(NICKNAME);
        userRepository.save(user);
        flushAndClear();

        boolean exists = userRepository.existsByNickname(NICKNAME);

        assertThat(exists).isTrue();
    }

    @Test
    void 닉네임에_해당하는_회원이_없으면_존재하지_않는다고_응답한다() {
        boolean exists = userRepository.existsByNickname("unknown-member");

        assertThat(exists).isFalse();
    }

    @Test
    void 탈퇴한_회원도_닉네임_존재_여부_확인_대상에_포함한다() {
        String withdrawnUserNickname = "withdrawn-member";
        User user = createUser(
                WITHDRAWN_USER_EMAIL,
                withdrawnUserNickname,
                Withdrawal.completedWith(WITHDRAWN_AT, WITHDRAWAL_REASON)
        );
        userRepository.save(user);
        flushAndClear();

        boolean exists = userRepository.existsByNickname(withdrawnUserNickname);

        assertThat(exists).isTrue();
    }

    @Test
    void 자기_자신의_닉네임은_중복_확인_대상에서_제외한다() {
        User user = createUser(NICKNAME);
        User savedUser = userRepository.save(user);
        flushAndClear();

        boolean exists = userRepository.existsByNicknameNotAndUserIdNot(NICKNAME, savedUser.getUserId());

        assertThat(exists).isFalse();
    }

    @Test
    void 다른_회원이_사용_중인_닉네임이면_중복으로_확인한다() {
        String anotherUserNickname = "another-member";
        User user = createUser(NICKNAME);
        User anotherUser = createUser(createEmail("another@test.com"), anotherUserNickname, Withdrawal.none());
        User savedUser = userRepository.save(user);
        userRepository.save(anotherUser);
        flushAndClear();

        boolean exists = userRepository.existsByNicknameNotAndUserIdNot(anotherUserNickname, savedUser.getUserId());

        assertThat(exists).isTrue();
    }

    @Test
    void 자기_자신을_제외하고_닉네임에_해당하는_회원이_없으면_존재하지_않는다고_응답한다() {
        User user = createUser(NICKNAME);
        User savedUser = userRepository.save(user);
        flushAndClear();

        boolean exists = userRepository.existsByNicknameNotAndUserIdNot("unknown-member", savedUser.getUserId());

        assertThat(exists).isFalse();
    }
}
