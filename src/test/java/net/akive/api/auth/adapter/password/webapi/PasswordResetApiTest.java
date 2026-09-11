package net.akive.api.auth.adapter.password.webapi;

import net.akive.api.auth.adapter.password.webapi.dto.PasswordResetRequest;
import net.akive.api.auth.adapter.password.webapi.fixture.PasswordResetRequestFixture;
import net.akive.api.auth.application.password.provided.PasswordModifier;
import net.akive.api.auth.application.password.provided.dto.PasswordResetCommand;
import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.auth.adapter.password.webapi.fixture.PasswordResetRequestFixture.createPasswordResetRequest;
import static net.akive.api.auth.application.password.fixture.PasswordCommandFixture.createPasswordResetCommand;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_NOT_VERIFIED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(PasswordResetApi.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetApiTest extends PublicApiTestSupport {

    private static final String PASSWORD_RESET_URL = "/auth/password-reset";

    @MockitoBean
    PasswordModifier passwordModifier;

    @Test
    void 패스워드를_리셋한다() {
        PasswordResetRequest request = createPasswordResetRequest();
        PasswordResetCommand command = createPasswordResetCommand();

        patchJson(PASSWORD_RESET_URL, toJson(request)).hasStatus(NO_CONTENT);

        then(passwordModifier).should().reset(command);
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordResetRequests")
    void 패스워드_리셋_요청값이_올바르지_않으면_400을_응답한다(PasswordResetRequest request) {
        patchJson(PASSWORD_RESET_URL, toJson(request)).hasStatus(BAD_REQUEST);

        then(passwordModifier).should(never()).reset(any(PasswordResetCommand.class));
    }

    @Test
    void 패스워드_리셋_시_이메일이_인증되지_않으면_400을_응답한다() {
        PasswordResetRequest request = createPasswordResetRequest();
        PasswordResetCommand command = createPasswordResetCommand();
        willThrow(new BusinessRuleViolationException(EMAIL_NOT_VERIFIED))
                .given(passwordModifier)
                .reset(command);

        MvcTestResultAssert result = patchJson(PASSWORD_RESET_URL, toJson(request))
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, EMAIL_NOT_VERIFIED);
        then(passwordModifier).should().reset(command);
    }

    @Test
    void 패스워드_리셋_시_회원을_조회할_수_없으면_404를_응답한다() {
        PasswordResetRequest request = createPasswordResetRequest();
        PasswordResetCommand command = createPasswordResetCommand();
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(passwordModifier)
                .reset(command);

        MvcTestResultAssert result = patchJson(PASSWORD_RESET_URL, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(passwordModifier).should().reset(command);
    }

    private static Stream<PasswordResetRequest> invalidPasswordResetRequests() {
        return PasswordResetRequestFixture.invalidPasswordResetRequests();
    }
}