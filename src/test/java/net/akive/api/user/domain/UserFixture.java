package net.akive.api.user.domain;

import net.akive.api.common.domain.Email;

public class UserFixture {

    public static User createUser() {
        return User.signUpByEmail(
                new Email("test@test.com"),
                "USER@abcde",
                "password123!",
                RoleType.USER
        );
    }
}
