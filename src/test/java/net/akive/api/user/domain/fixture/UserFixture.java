package net.akive.api.user.domain.fixture;

import net.akive.api.common.domain.Email;
import net.akive.api.user.domain.*;

import java.time.Instant;

import static net.akive.api.common.domain.EmailFixture.createEmail;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;

public class UserFixture {

    private UserFixture() {
    }

    public static final Long USER_ID = 1L;
    public static final Long ANOTHER_USER_ID = 2L;
    public static final Long UNKNOWN_USER_ID = Long.MAX_VALUE;
    public static final Email EMAIL = createEmail();
    public static final Email ANOTHER_EMAIL = createEmail("another@test.com");
    public static final String ANOTHER_NICKNAME = "another-member";
    public static final UserRole USER_ROLE = UserRole.MEMBER;
    public static final LoginType LOGIN_TYPE = LoginType.EMAIL;
    public static final String PASSWORD = "password123!";
    public static final String PASSWORD_HASH = "password-hash";
    public static final String TARGET_PASSWORD = "targetPassword123!";
    public static final String TARGET_PASSWORD_HASH = "target-password-hash";
    public static final Instant CREATED_AT = Instant.parse("2026-09-18T12:00:01Z");

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
        User user = User.signUpByEmail(email, USER_ROLE, PASSWORD_HASH, nickname);

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

    public static User createReconstructedUser(Long userId, UserDetail userDetail) {
        return createReconstructedUser(userId, PASSWORD_HASH, userDetail);
    }

    public static User createReconstructedUser(Long userId, String passwordHash) {
        return createReconstructedUser(userId, passwordHash, UserDetail.initial(NICKNAME));
    }

    public static User createReconstructedUser(Long userId, String passwordHash, UserDetail userDetail) {
        return User.reconstruct(
                userId,
                EMAIL,
                USER_ROLE,
                LOGIN_TYPE,
                passwordHash,
                userDetail,
                Withdrawal.none(),
                CREATED_AT
        );
    }
}
