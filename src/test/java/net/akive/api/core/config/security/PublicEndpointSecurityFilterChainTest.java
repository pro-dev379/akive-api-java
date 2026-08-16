package net.akive.api.core.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.adapter.login.webapi.LoginApi;
import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.application.login.provided.LoginProcessor;
import org.junit.jupiter.api.Test;
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

import static net.akive.api.auth.domain.token.IssuedTokenFixture.createIssuedToken;
import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_ACCESS_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebMvcTest(controllers = LoginApi.class)
@AutoConfigureMockMvc
@Import({ SecurityConfig.class, TokenAuthenticationEntryPoint.class })
class PublicEndpointSecurityFilterChainTest {

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
        LoginByEmailRequest request = new LoginByEmailRequest("test@test.com", "password");

        given(loginProcessor.loginByEmail(any()))
                .willReturn(createIssuedToken("access-token", "refresh-token"));

        mvcTester.post()
                .uri("/login/email")
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

        result.bodyJson().extractingPath("$.code").isEqualTo(INVALID_ACCESS_TOKEN.getCode());
        result.bodyJson().extractingPath("$.message").isEqualTo(INVALID_ACCESS_TOKEN.getMessage());
    }
}
