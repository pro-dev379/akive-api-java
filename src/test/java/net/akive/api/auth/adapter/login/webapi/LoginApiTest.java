package net.akive.api.auth.adapter.login.webapi;

import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.adapter.login.webapi.fixture.LoginByEmailRequestFixture;
import net.akive.api.auth.application.login.provided.LoginProcessor;
import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.UUID;
import java.util.stream.Stream;

import static net.akive.api.auth.adapter.login.webapi.fixture.LoginByEmailRequestFixture.createLoginByEmailRequest;
import static net.akive.api.auth.domain.token.AccessTokenFixture.ACCESS_TOKEN_VALUE;
import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.auth.domain.token.RefreshTokenFixture.REFRESH_TOKEN_VALUE;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(LoginApi.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginApiTest extends PublicApiTestSupport {

    private static final String LOGIN_BY_EMAIL_URL = "/auth/login/email";

    @MockitoBean
    LoginProcessor loginProcessor;

    @Test
    void 이메일과_패스워드로_로그인하면_토큰을_응답한다() {
        UUID clientInstanceId = UUID.randomUUID();
        LoginByEmailRequest request = createLoginByEmailRequest();
        LoginByEmailCommand command = LoginByEmailCommand.of(
                clientInstanceId,
                request.email(),
                request.password()
        );
        IssuedToken issuedToken = createIssuedToken(ACCESS_TOKEN_VALUE, REFRESH_TOKEN_VALUE);

        given(loginProcessor.loginByEmail(command)).willReturn(issuedToken);

        MvcTestResultAssert result = postLoginByEmail(clientInstanceId.toString(), request).hasStatus(OK);

        result.bodyJson().extractingPath("$.accessToken").isEqualTo(ACCESS_TOKEN_VALUE);
        result.bodyJson().extractingPath("$.refreshToken").isEqualTo(REFRESH_TOKEN_VALUE);

        then(loginProcessor).should().loginByEmail(command);
    }

    @ParameterizedTest
    @MethodSource("invalidLoginRequests")
    void 이메일_로그인_요청값이_올바르지_않으면_400을_응답한다(LoginByEmailRequest request) {
        postLoginByEmail(UUID.randomUUID().toString(), request).hasStatus(BAD_REQUEST);

        then(loginProcessor).should(never()).loginByEmail(any(LoginByEmailCommand.class));
    }

    @ParameterizedTest
    @MethodSource("invalidClientInstanceIdHeaders")
    void 이메일_로그인_요청_시_클라이언트_식별자가_올바르지_않으면_400을_응답한다(String clientInstanceIdHeader) {
        postLoginByEmail(clientInstanceIdHeader, createLoginByEmailRequest()).hasStatus(BAD_REQUEST);

        then(loginProcessor).should(never()).loginByEmail(any(LoginByEmailCommand.class));
    }

    @Test
    void 이메일_로그인_요청_시_인증에_실패하면_401을_응답한다() {
        UUID clientInstanceId = UUID.randomUUID();
        LoginByEmailRequest request = createLoginByEmailRequest();
        LoginByEmailCommand command = LoginByEmailCommand.of(
                clientInstanceId,
                request.email(),
                request.password()
        );

        willThrow(new AuthenticationException(INVALID_EMAIL_OR_PASSWORD))
                .given(loginProcessor)
                .loginByEmail(command);

        MvcTestResultAssert result = postLoginByEmail(clientInstanceId.toString(), request).hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_EMAIL_OR_PASSWORD);

        then(loginProcessor).should().loginByEmail(command);
    }

    private MvcTestResultAssert postLoginByEmail(String clientInstanceIdHeader, LoginByEmailRequest request) {
        var requestBuilder = mvcTester.post()
                .uri(LOGIN_BY_EMAIL_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request));

        if (clientInstanceIdHeader != null) {
            requestBuilder.header("X-Client-Instance-Id", clientInstanceIdHeader);
        }

        return requestBuilder.assertThat();
    }

    private static Stream<LoginByEmailRequest> invalidLoginRequests() {
        return LoginByEmailRequestFixture.invalidLoginRequests();
    }

    private static Stream<String> invalidClientInstanceIdHeaders() {
        return Stream.of(
                null,
                "",
                "invalid-uuid"
        );
    }
}
