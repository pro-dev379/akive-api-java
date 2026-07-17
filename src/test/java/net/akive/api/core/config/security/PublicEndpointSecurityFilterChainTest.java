package net.akive.api.core.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.application.dto.result.login.LoginResult;
import net.akive.api.auth.application.port.in.login.LoginByEmailUseCase;
import net.akive.api.auth.presentation.LoginController;
import net.akive.api.auth.presentation.dto.request.login.LoginByEmailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static net.akive.api.auth.exception.TokenErrorCode.INVALID_ACCESS_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class)
@AutoConfigureMockMvc
@Import({
        SecurityConfig.class,
        TokenAuthenticationEntryPoint.class,
})
class PublicEndpointSecurityFilterChainTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    JwtDecoder accessTokenJwtDecoder;

    @MockitoBean
    LoginByEmailUseCase loginByEmailUseCase;

    @Test
    void 퍼블릭_엔드포인트는_Authorization_헤더가_있어도_액세스_토큰을_검증하지_않는다() throws Exception {
        given(loginByEmailUseCase.login(any()))
                .willReturn(new LoginResult("access-token", "refresh-token"));

        mockMvc.perform(post("/login/email")
                        .header(AUTHORIZATION, "Bearer invalid-access-token")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginByEmailRequest("test@test.com", "password")
                        )))
                .andExpect(status().isOk());

        then(accessTokenJwtDecoder).shouldHaveNoInteractions();
    }

    @Test
    void 프라이빗_엔드포인트는_Authorization_헤더가_있으면_액세스_토큰을_검증한다() throws Exception {
        given(accessTokenJwtDecoder.decode("invalid-access-token"))
                .willThrow(new BadJwtException("Invalid token"));

        mockMvc.perform(get("/private")
                        .header(AUTHORIZATION, "Bearer invalid-access-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(INVALID_ACCESS_TOKEN.getCode()))
                .andExpect(jsonPath("$.message").value(INVALID_ACCESS_TOKEN.getMessage()));
    }
}
