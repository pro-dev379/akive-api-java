package net.akive.api.auth.adapter.login.webapi.dto.request;

import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.auth.domain.login.LegacyUserPasswordFixture.RAW_PASSWORD;

public class LoginByEmailRequestFixture {

    public static LoginByEmailRequest createLoginByEmailRequest() {
        return createLoginByEmailRequest(EMAIL_ADDRESS, RAW_PASSWORD);
    }

    public static LoginByEmailRequest createLoginByEmailRequest(String email, String password) {
        return new LoginByEmailRequest(email, password);
    }
}
