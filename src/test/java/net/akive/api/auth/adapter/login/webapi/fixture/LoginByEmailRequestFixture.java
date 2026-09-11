package net.akive.api.auth.adapter.login.webapi.fixture;

import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;

import java.util.stream.Stream;

import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD_HASH;

public class LoginByEmailRequestFixture {

    public static LoginByEmailRequest createLoginByEmailRequest() {
        return createLoginByEmailRequest(EMAIL_ADDRESS, PASSWORD_HASH);
    }

    public static LoginByEmailRequest createLoginByEmailRequest(String email, String password) {
        return new LoginByEmailRequest(email, password);
    }

    public static Stream<LoginByEmailRequest> invalidLoginRequests() {
        return Stream.of(
                createLoginByEmailRequest(null, PASSWORD),
                createLoginByEmailRequest("", PASSWORD),
                createLoginByEmailRequest(" ", PASSWORD),
                createLoginByEmailRequest("invalid-email", PASSWORD),
                createLoginByEmailRequest("test@", PASSWORD),
                createLoginByEmailRequest("@test.com", PASSWORD),
                createLoginByEmailRequest(EMAIL_ADDRESS, null),
                createLoginByEmailRequest(EMAIL_ADDRESS, ""),
                createLoginByEmailRequest(EMAIL_ADDRESS, " ")
        );
    }
}
