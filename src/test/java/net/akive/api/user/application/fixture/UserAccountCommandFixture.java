package net.akive.api.user.application.fixture;

import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;

import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;

public class UserAccountCommandFixture {

    public static UserSignUpByEmailCommand createUserSignUpByEmailCommand() {
        return new UserSignUpByEmailCommand(EMAIL, PASSWORD, NICKNAME);
    }
}
