package net.akive.api.auth.application.login.fixture;

import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.common.domain.ClientInstanceId;
import net.akive.api.common.domain.Email;

import static net.akive.api.common.domain.ClientInstanceIdFixture.createClientInstanceId;
import static net.akive.api.user.domain.fixture.UserFixture.EMAIL;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD_HASH;

public class LoginCommandFixture {

    private LoginCommandFixture() {
    }

    public static LoginByEmailCommand createLoginByEmailCommand() {
        return createLoginByEmailCommand(createClientInstanceId(), EMAIL, PASSWORD_HASH);
    }

    public static LoginByEmailCommand createLoginByEmailCommand(
            ClientInstanceId clientInstanceId,
            Email email,
            String password
    ) {
        return new LoginByEmailCommand(clientInstanceId, email, password);
    }
}
