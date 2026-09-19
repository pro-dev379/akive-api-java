package net.akive.api.auth.adapter.password.webapi.fixture;

import net.akive.api.auth.adapter.password.webapi.dto.PasswordResetRequest;

import java.util.stream.Stream;

import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.user.domain.fixture.UserFixture.TARGET_PASSWORD;

public class PasswordResetRequestFixture {

    private PasswordResetRequestFixture() {
    }

    public static PasswordResetRequest createPasswordResetRequest() {
        return new PasswordResetRequest(EMAIL_ADDRESS, TARGET_PASSWORD);
    }

    public static Stream<PasswordResetRequest> invalidPasswordResetRequests() {
        return Stream.of(
                new PasswordResetRequest(null, TARGET_PASSWORD),
                new PasswordResetRequest("", TARGET_PASSWORD),
                new PasswordResetRequest(" ", TARGET_PASSWORD),
                new PasswordResetRequest("invalid-email", TARGET_PASSWORD),
                new PasswordResetRequest("test@", TARGET_PASSWORD),
                new PasswordResetRequest("@test.com", TARGET_PASSWORD),
                new PasswordResetRequest(EMAIL_ADDRESS, null),
                new PasswordResetRequest(EMAIL_ADDRESS, ""),
                new PasswordResetRequest(EMAIL_ADDRESS, " "),
                new PasswordResetRequest(EMAIL_ADDRESS, "short1!"),
                new PasswordResetRequest(EMAIL_ADDRESS, "password"),
                new PasswordResetRequest(EMAIL_ADDRESS, "password123")
        );
    }
}
