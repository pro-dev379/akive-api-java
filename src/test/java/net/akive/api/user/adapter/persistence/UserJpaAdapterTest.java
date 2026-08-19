package net.akive.api.user.adapter.persistence;

import net.akive.api.common.domain.Email;
import net.akive.api.support.RepositoryTestSupport;
import net.akive.api.user.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.akive.api.common.domain.EmailFixture.createEmail;
import static org.assertj.core.api.Assertions.assertThat;

class UserJpaAdapterTest extends RepositoryTestSupport {

    private static final Email EMAIL = createEmail("user@test.com");
    private static final RoleType ROLE_TYPE = RoleType.MEMBER;
    private static final LoginType LOGIN_TYPE = LoginType.EMAIL;
    private static final Email LEFT_USER_EMAIL = createEmail("left-user@test.com");
    private static final String NICKNAME = "member@abcdef1234";
    private static final String LEFT_USER_NICKNAME = "left-member";
    private static final String PASSWORD_HASH = "password-hash";
    private static final String IMG_URL = "https://example.com/image.png";
    private static final String AIM = "목표";

    @Test
    void 회원을_저장한다() {
        User user = User.reconstruct(
                null,
                EMAIL,
                ROLE_TYPE,
                LOGIN_TYPE,
                PASSWORD_HASH,
                UserDetail.of(NICKNAME, IMG_URL, AIM),
                Leave.none()
        );

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(savedUser.getRoleType()).isEqualTo(ROLE_TYPE);
        assertThat(savedUser.getLoginType()).isEqualTo(LOGIN_TYPE);
        assertThat(savedUser.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(savedUser.getUserDetail().nickname()).isEqualTo(NICKNAME);
        assertThat(savedUser.getUserDetail().imgUrl()).isEqualTo(IMG_URL);
        assertThat(savedUser.getUserDetail().aim()).isEqualTo(AIM);
        assertThat(savedUser.getLeave()).isEqualTo(Leave.none());
    }

    @Test
    void 회원_식별자로_탈퇴하지_않은_회원을_조회한다() {
        User user = createUser(EMAIL, NICKNAME, Leave.none());
        User savedUser = userRepository.save(user);

        Optional<User> found = userRepository.findByUserIdAndLeaveFalse(savedUser.getUserId());

        assertThat(found).contains(savedUser);
    }

    @Test
    void 탈퇴한_회원은_회원_식별자로_조회되지_않는다() {
        User user = createUser(LEFT_USER_EMAIL, LEFT_USER_NICKNAME, Leave.completed("탈퇴 사유"));
        User savedUser = userRepository.save(user);

        Optional<User> found = userRepository.findByUserIdAndLeaveFalse(savedUser.getUserId());

        assertThat(found).isEmpty();
    }

    @Test
    void 이메일로_탈퇴하지_않은_회원을_조회한다() {
        User user = createUser(EMAIL, NICKNAME, Leave.none());
        User savedUser = userRepository.save(user);

        Optional<User> found = userRepository.findByEmailAndLeaveFalse(EMAIL);

        assertThat(found).contains(savedUser);
    }

    @Test
    void 탈퇴한_회원은_이메일로_조회되지_않는다() {
        User user = createUser(LEFT_USER_EMAIL, LEFT_USER_NICKNAME, Leave.completed("탈퇴 사유"));
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmailAndLeaveFalse(LEFT_USER_EMAIL);

        assertThat(found).isEmpty();
    }

    @Test
    void 이메일로_회원을_조회한다() {
        User user = createUser(EMAIL, NICKNAME, Leave.none());
        User savedUser = userRepository.save(user);

        Optional<User> found = userRepository.findByEmail(EMAIL);

        assertThat(found).contains(savedUser);
    }

    @Test
    void 닉네임으로_회원을_조회한다() {
        User user = createUser(EMAIL, NICKNAME, Leave.none());
        User savedUser = userRepository.save(user);

        Optional<User> found = userRepository.findByNickname(NICKNAME);

        assertThat(found).contains(savedUser);
    }

    private User createUser(Email email, String nickname, Leave leave) {
        return User.reconstruct(
                null,
                email,
                ROLE_TYPE,
                LOGIN_TYPE,
                PASSWORD_HASH,
                UserDetail.initial(nickname),
                leave
        );
    }
}
