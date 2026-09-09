package net.akive.api.user.adapter.persistence;

import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.UserDetail;
import net.akive.api.user.domain.Withdrawal;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWN_USER_EMAIL;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.createWithdrawnUser;
import static org.assertj.core.api.Assertions.assertThat;

class UserJpaAdapterTest extends RepositoryTestSupport {

    @Test
    void 회원을_저장한다() {
        User user = User.reconstruct(
                null,
                EMAIL,
                ROLE_TYPE,
                LOGIN_TYPE,
                PASSWORD_HASH,
                UserDetail.of(NICKNAME, IMG_URL, AIM),
                Withdrawal.none()
        );

        User savedUser = userRepository.save(user);
        flushAndClear();

        User foundUser = userRepository.findByUserIdAndNotWithdrawn(savedUser.getUserId()).orElseThrow();

        assertThat(foundUser.getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundUser.getEmail()).isEqualTo(EMAIL);
        assertThat(foundUser.getRoleType()).isEqualTo(ROLE_TYPE);
        assertThat(foundUser.getLoginType()).isEqualTo(LOGIN_TYPE);
        assertThat(foundUser.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(foundUser.getUserDetail().nickname()).isEqualTo(NICKNAME);
        assertThat(foundUser.getUserDetail().imgUrl()).isEqualTo(IMG_URL);
        assertThat(foundUser.getUserDetail().aim()).isEqualTo(AIM);
        assertThat(foundUser.getWithdrawal()).isEqualTo(Withdrawal.none());
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회한다() {
        User user = createUser(EMAIL, NICKNAME, Withdrawal.none());
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
        User user = createUser(EMAIL, NICKNAME, Withdrawal.none());
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
    void 이메일로_회원을_조회한다() {
        User user = createUser(EMAIL, NICKNAME, Withdrawal.none());
        User savedUser = userRepository.save(user);
        flushAndClear();

        Optional<User> foundUser = userRepository.findByEmail(EMAIL);

        assertThat(foundUser).contains(savedUser);
    }

    @Test
    void 닉네임으로_회원을_조회한다() {
        User user = createUser(EMAIL, NICKNAME, Withdrawal.none());
        User savedUser = userRepository.save(user);
        flushAndClear();

        Optional<User> foundUser = userRepository.findByNickname(NICKNAME);

        assertThat(foundUser).contains(savedUser);
    }
}
