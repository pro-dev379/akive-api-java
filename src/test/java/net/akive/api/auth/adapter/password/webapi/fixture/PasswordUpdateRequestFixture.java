package net.akive.api.auth.adapter.password.webapi.fixture;

import net.akive.api.auth.adapter.password.webapi.dto.PasswordUpdateRequest;

import java.util.stream.Stream;

import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;
import static net.akive.api.user.domain.fixture.UserFixture.TARGET_PASSWORD;

public class PasswordUpdateRequestFixture {

    private PasswordUpdateRequestFixture() {
    }

    public static PasswordUpdateRequest createPasswordUpdateRequest() {
        return new PasswordUpdateRequest(PASSWORD, TARGET_PASSWORD);
    }

    public static Stream<PasswordUpdateRequest> invalidPasswordUpdateRequests() {
        return Stream.of(
                new PasswordUpdateRequest(null, TARGET_PASSWORD),
                new PasswordUpdateRequest("", TARGET_PASSWORD),
                new PasswordUpdateRequest(" ", TARGET_PASSWORD),
                new PasswordUpdateRequest(PASSWORD, null),
                new PasswordUpdateRequest(PASSWORD, ""),
                new PasswordUpdateRequest(PASSWORD, " "),
                new PasswordUpdateRequest(PASSWORD, "short1!"),
                new PasswordUpdateRequest(PASSWORD, "password"),
                new PasswordUpdateRequest(PASSWORD, "password123")
        );
    }
}
