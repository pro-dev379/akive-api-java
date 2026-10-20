package net.akive.api.core.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.adapter.login.webapi.LoginApi;
import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.application.login.provided.LoginProcessor;
import net.akive.api.common.exception.ApplicationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.UUID;

import static net.akive.api.auth.adapter.login.webapi.fixture.LoginByEmailRequestFixture.createLoginByEmailRequest;
import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.common.exception.business.AuthenticationError.INVALID_ACCESS_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(controllers = LoginApi.class)
@AutoConfigureMockMvc
@Import({ SecurityConfig.class, TokenAuthenticationEntryPoint.class })
class SecurityConfigTest {

    @Autowired
    MockMvcTester mvcTester;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    JwtDecoder accessTokenJwtDecoder;

    @MockitoBean
    LoginProcessor loginProcessor;

    @Test
    void 퍼블릭_엔드포인트는_Authorization_헤더가_있어도_액세스_토큰을_검증하지_않는다() throws JsonProcessingException {
        LoginByEmailRequest request = createLoginByEmailRequest();

        given(loginProcessor.loginByEmail(any()))
                .willReturn(createIssuedToken());

        mvcTester.post()
                .uri("/auth/login/email")
                .header(AUTHORIZATION, "Bearer invalid-access-token")
                .header("X-Client-Instance-Id", UUID.randomUUID().toString())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .assertThat()
                .hasStatus(OK);

        then(accessTokenJwtDecoder).shouldHaveNoInteractions();
    }

    @Test
    void 프라이빗_엔드포인트는_액세스_토큰을_검증한다() {
        given(accessTokenJwtDecoder.decode("invalid-access-token"))
                .willThrow(new BadJwtException("Invalid token"));

        MvcTestResultAssert result = mvcTester.get()
                .uri("/private")
                .header(AUTHORIZATION, "Bearer invalid-access-token")
                .assertThat()
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, INVALID_ACCESS_TOKEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:3001",
            "https://dev-www.akive.net",
            "https://www.akive.net"
    })
    void 허용된_출처의_CORS_사전_요청을_허용한다(String origin) {
        mvcTester.options()
                .uri("/private")
                .header(ORIGIN, origin)
                .header(ACCESS_CONTROL_REQUEST_METHOD, POST.name())
                .assertThat()
                .hasStatus(OK)
                .hasHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
    }

    @Test
    void 허용되지_않은_출처의_CORS_사전_요청을_거부한다() {
        mvcTester.options()
                .uri("/private")
                .header(ORIGIN, "https://malicious.example")
                .header(ACCESS_CONTROL_REQUEST_METHOD, POST.name())
                .assertThat()
                .hasStatus(FORBIDDEN);
    }

    private void assertErrorResponse(MvcTestResultAssert result, ApplicationError error) {
        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo(error.getCode());
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo(error.getMessage());
    }
}
