package net.akive.api.user.domain;

import static net.akive.api.common.domain.EmailFixture.createEmail;

public class UserFixture {

    public static User createUser() {
        return User.signUpByEmail(
                createEmail(),
                "USER@abcde",
                "password123!",
                RoleType.USER
        );
    }

    public static User createUser(Long userId) {
        return User.reconstruct(
                userId,
                createEmail(),
                RoleType.USER,
                LoginType.EMAIL,
                "USER@abcde",
                "password123!",
                null,
                null,
                false,
                null
        );
    }
}
