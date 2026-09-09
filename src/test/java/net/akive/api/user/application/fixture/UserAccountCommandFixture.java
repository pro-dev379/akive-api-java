package net.akive.api.user.application.fixture;

import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;

import static net.akive.api.user.domain.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.UserFixture.EMAIL;
import static net.akive.api.user.domain.UserFixture.PASSWORD;

public class UserAccountCommandFixture {

    public static UserSignUpByEmailCommand createUserSignUpByEmailCommand() {
        return new UserSignUpByEmailCommand(EMAIL, PASSWORD, NICKNAME);
    }
}
