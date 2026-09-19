package net.akive.api.auth.application.password.fixture;

import net.akive.api.auth.application.password.provided.dto.PasswordResetCommand;
import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;

import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;
import static net.akive.api.user.domain.fixture.UserFixture.*;

public class PasswordCommandFixture {

    private PasswordCommandFixture() {
    }

    public static PasswordUpdateCommand createPasswordUpdateCommand() {
        return createPasswordUpdateCommand(USER_ID);
    }

    public static PasswordUpdateCommand createPasswordUpdateCommand(Long userId) {
        return new PasswordUpdateCommand(userId, PASSWORD, TARGET_PASSWORD);
    }

    public static PasswordResetCommand createPasswordResetCommand() {
        return new PasswordResetCommand(EMAIL, TARGET_PASSWORD);
    }
}
