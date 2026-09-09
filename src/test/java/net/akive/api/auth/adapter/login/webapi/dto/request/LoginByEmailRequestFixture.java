package net.akive.api.auth.adapter.login.webapi.dto.request;

import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.user.domain.UserFixture.PASSWORD_HASH;

public class LoginByEmailRequestFixture {

    public static LoginByEmailRequest createLoginByEmailRequest() {
        return createLoginByEmailRequest(EMAIL_ADDRESS, PASSWORD_HASH);
    }

    public static LoginByEmailRequest createLoginByEmailRequest(String email, String password) {
        return new LoginByEmailRequest(email, password);
    }
}
