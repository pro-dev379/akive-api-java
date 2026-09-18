package net.akive.api.user.domain.fixture;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.*;

import java.time.LocalDateTime;

import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;

public class UserFixture {

    public static final Long USER_ID = 1L;
    public static final Long ANOTHER_USER_ID = 2L;
    public static final Long UNKNOWN_USER_ID = Long.MAX_VALUE;
    public static final Email EMAIL = createEmail();
    public static final RoleType ROLE_TYPE = RoleType.MEMBER;
    public static final LoginType LOGIN_TYPE = LoginType.EMAIL;
    public static final String PASSWORD = "password123!";
    public static final String PASSWORD_HASH = "password-hash";
    public static final String TARGET_PASSWORD = "targetPassword123!";
    public static final String TARGET_PASSWORD_HASH = "target-password-hash";
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 9, 18, 12, 0, 1);

    public static User createUser() {
        return createUser(EMAIL, NICKNAME);
    }

    public static User createUser(Email email) {
        return createUser(email, NICKNAME);
    }

    public static User createUser(String nickname) {
        return createUser(EMAIL, nickname);
    }

    public static User createUser(Email email, String nickname) {
        return createUser(email, nickname, Withdrawal.none());
    }

    public static User createUser(Email email, String nickname, Withdrawal withdrawal) {
        User user = User.signUpByEmail(email, ROLE_TYPE, PASSWORD_HASH, nickname);

        if (withdrawal.isWithdrawn()) {
            user.withdraw(withdrawal.withdrawnAt(), withdrawal.withdrawalReason());
        }

        return user;
    }

    public static User createUser(Long userId) {
        return createReconstructedUser(userId);
    }

    public static User createUser(Long userId, String passwordHash) {
        return createReconstructedUser(userId, passwordHash);
    }

    public static User createReconstructedUser() {
        return createReconstructedUser(USER_ID);
    }

    public static User createReconstructedUser(Long userId) {
        return createReconstructedUser(userId, PASSWORD_HASH);
    }

    public static User createReconstructedUser(Long userId, String passwordHash) {
        return User.reconstruct(
                userId,
                EMAIL,
                ROLE_TYPE,
                LOGIN_TYPE,
                passwordHash,
                UserDetail.initial(NICKNAME),
                Withdrawal.none(),
                CREATED_AT
        );
    }
}
