package net.akive.api.auth.adapter.password.webapi;

import net.akive.api.auth.adapter.password.webapi.dto.PasswordUpdateRequest;
import net.akive.api.auth.adapter.password.webapi.fixture.PasswordUpdateRequestFixture;
import net.akive.api.auth.application.password.provided.PasswordModifier;
import net.akive.api.auth.application.password.provided.dto.PasswordUpdateCommand;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PrivateApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.auth.adapter.password.webapi.fixture.PasswordUpdateRequestFixture.createPasswordUpdateRequest;
import static net.akive.api.auth.application.password.fixture.PasswordCommandFixture.createPasswordUpdateCommand;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.fixture.UserFixture.UNKNOWN_USER_ID;
import static net.akive.api.user.domain.fixture.UserFixture.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(PasswordApi.class)
class PasswordApiTest extends PrivateApiTestSupport {

    private static final String PASSWORD_UPDATE_URL = "/auth/me/password";

    @MockitoBean
    PasswordModifier passwordModifier;

    @Test
    void 패스워드를_변경한다() {
        PasswordUpdateRequest request = createPasswordUpdateRequest();
        PasswordUpdateCommand command = createPasswordUpdateCommand();

        patchJson(PASSWORD_UPDATE_URL, USER_ID, toJson(request)).hasStatus(NO_CONTENT);

        then(passwordModifier).should().update(command);
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordUpdateRequests")
    void 패스워드_변경_요청값이_올바르지_않으면_400을_응답한다(PasswordUpdateRequest request) {
        patchJson(PASSWORD_UPDATE_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(passwordModifier).should(never()).update(any(PasswordUpdateCommand.class));
    }

    @Test
    void 회원이_입력한_현재_패스워드와_기존_패스워드가_일치하지_않으면_401을_응답한다() {
        PasswordUpdateRequest request = createPasswordUpdateRequest();
        PasswordUpdateCommand command = createPasswordUpdateCommand();
        willThrow(new AuthenticationException(INVALID_EMAIL_OR_PASSWORD))
                .given(passwordModifier)
                .update(command);

        MvcTestResultAssert result = patchJson(PASSWORD_UPDATE_URL, USER_ID, toJson(request))
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_EMAIL_OR_PASSWORD);
        then(passwordModifier).should().update(command);
    }

    @Test
    void 패스워드_변경_시_회원을_조회할_수_없으면_404를_응답한다() {
        PasswordUpdateRequest request = createPasswordUpdateRequest();
        PasswordUpdateCommand command = createPasswordUpdateCommand(UNKNOWN_USER_ID);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(passwordModifier)
                .update(command);

        MvcTestResultAssert result = patchJson(PASSWORD_UPDATE_URL, UNKNOWN_USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(passwordModifier).should().update(command);
    }

    private static Stream<PasswordUpdateRequest> invalidPasswordUpdateRequests() {
        return PasswordUpdateRequestFixture.invalidPasswordUpdateRequests();
    }
}
