package net.akive.api.auth.application.password.fixture;

import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;

import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;

public class PasswordCommandFixture {

    public static final String TARGET_PASSWORD = "targetPassword123!";
    public static final String TARGET_PASSWORD_HASH = "target-password-hash";

    public static PasswordUpdateCommand createPasswordUpdateCommand() {
        return createPasswordUpdateCommand(USER_ID);
    }

    public static PasswordUpdateCommand createPasswordUpdateCommand(Long userId) {
        return new PasswordUpdateCommand(userId, PASSWORD, TARGET_PASSWORD);
    }
}
