package net.akive.api.auth.adapter.login.webapi;

import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand;
import net.akive.api.auth.domain.token.IssuedToken;
import net.akive.api.common.exception.business.AuthenticationException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.UUID;
import java.util.stream.Stream;

import static net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequestFixture.createLoginByEmailRequest;
import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_EMAIL_OR_PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class LoginApiTest extends PublicApiTestSupport {

    private static final String LOGIN_BY_EMAIL_URL = "/login/email";

    @Test
    void 이메일과_패스워드로_로그인하면_토큰을_응답한다() {
        UUID clientInstanceId = UUID.randomUUID();
        LoginByEmailRequest request = createLoginByEmailRequest();
        LoginByEmailCommand command = LoginByEmailCommand.of(
                clientInstanceId,
                request.email(),
                request.password()
        );
        IssuedToken issuedToken = createIssuedToken("access-token", "refresh-token");

        given(loginProcessor.loginByEmail(command)).willReturn(issuedToken);

        MvcTestResultAssert result = postLoginByEmail(clientInstanceId.toString(), request).hasStatus(OK);

        result.bodyJson().extractingPath("$.accessToken").isEqualTo("access-token");
        result.bodyJson().extractingPath("$.refreshToken").isEqualTo("refresh-token");

        then(loginProcessor).should().loginByEmail(command);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
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

        postLoginByEmail(clientInstanceId.toString(), request).hasStatus(UNAUTHORIZED);

        then(loginProcessor).should().loginByEmail(command);
    }

    private MvcTestResultAssert postLoginByEmail(String clientInstanceIdHeader, LoginByEmailRequest request) {
        var requestBuilder = mvcTester.post()
                .uri(LOGIN_BY_EMAIL_URL)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(toJson(request));

        if (clientInstanceIdHeader != null) {
            requestBuilder.header("X-Client-Instance-Id", clientInstanceIdHeader);
        }

        return requestBuilder.assertThat();
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize request body.", e);
        }
    }

    private static Stream<LoginByEmailRequest> invalidRequests() {
        return Stream.of(
                createLoginByEmailRequest(null, "password123!"),
                createLoginByEmailRequest("", "password123!"),
                createLoginByEmailRequest(" ", "password123!"),
                createLoginByEmailRequest("invalid-email", "password123!"),
                createLoginByEmailRequest("test@", "password123!"),
                createLoginByEmailRequest("@test.com", "password123!"),
                createLoginByEmailRequest("test@test.com", null),
                createLoginByEmailRequest("test@test.com", ""),
                createLoginByEmailRequest("test@test.com", " ")
        );
    }

    private static Stream<String> invalidClientInstanceIdHeaders() {
        return Stream.of(
                null,
                "",
                "invalid-uuid"
        );
    }
}
