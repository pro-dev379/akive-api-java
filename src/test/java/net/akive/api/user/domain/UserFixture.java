package net.akive.api.user.domain;

import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.EmailFixture.createEmail;

public class UserFixture {

    static final Email EMAIL = createEmail();
    static final RoleType ROLE_TYPE = RoleType.MEMBER;
    static final LoginType LOGIN_TYPE = LoginType.EMAIL;
    static final String NICKNAME = "member@abcdefg1234";
    static final String PASSWORD_HASH = "password-hash";

    public static User createUser() {
        return User.signUpByEmail(EMAIL, ROLE_TYPE, PASSWORD_HASH, NICKNAME);
    }

    public static User createUser(Email email, String nickname) {
        return User.reconstruct(
                null,
                email,
                ROLE_TYPE,
                LOGIN_TYPE,
                PASSWORD_HASH,
                UserDetail.initial(nickname),
                Leave.none()
        );
    }

    public static User createUser(Long userId) {
        return createPersistedUser(userId);
    }

    public static User createUser(Long userId, String passwordHash) {
        return createPersistedUser(userId, passwordHash);
    }

    public static User createPersistedUser(Long userId) {
        return createPersistedUser(userId, PASSWORD_HASH);
    }

    public static User createPersistedUser(Long userId, String passwordHash) {
        return User.reconstruct(
                userId,
                EMAIL,
                ROLE_TYPE,
                LOGIN_TYPE,
                passwordHash,
                UserDetail.initial(NICKNAME),
                Leave.none()
        );
    }
}
