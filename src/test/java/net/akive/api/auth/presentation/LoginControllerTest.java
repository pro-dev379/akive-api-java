package net.akive.api.auth.presentation;

import net.akive.api.auth.application.dto.result.LoginResult;
import net.akive.api.auth.domain.login.exception.LoginException;
import net.akive.api.auth.presentation.dto.request.LoginByEmailRequest;
import net.akive.api.support.ControllerTestSupport;
import org.junit.jupiter.api.Test;

import static net.akive.api.auth.domain.login.exception.LoginErrorCode.INVALID_EMAIL_OR_PASSWORD;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTest extends ControllerTestSupport {

    @Test
    void 이메일_로그인_요청시_올바른_이메일과_패스워드를_전달하면_토큰을_발급한다() throws Exception {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest("test@test.com", "password123!");
        LoginResult result = LoginResult.of("access-token", "refresh-token");

        given(loginByEmailUseCase.login(request.toCommand())).willReturn(result);

        // when
        mockMvc.perform(post("/login/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));


        then(loginByEmailUseCase).should().login(request.toCommand());
    }

    @Test
    void 이메일_로그인_요청시_이메일이_비어있으면_400을_응답한다() throws Exception {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest("", "password123!");

        // when
        mockMvc.perform(post("/login/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(loginByEmailUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_로그인_요청시_패스워드가_비어있으면_400을_응답한다() throws Exception {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest("test@test.com", "");

        // when
        mockMvc.perform(post("/login/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(loginByEmailUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_로그인_요청시_존재하지_않는_이메일이라면_401을_응답한다() throws Exception {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest("none@test.com", "password123!");
        given(loginByEmailUseCase.login(request.toCommand()))
                .willThrow(new LoginException(INVALID_EMAIL_OR_PASSWORD));


        // when
        mockMvc.perform(post("/login/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        then(loginByEmailUseCase).should().login(request.toCommand());
    }

    @Test
    void 이메일_로그인_요청시_패스워드가_일치하지_않으면_401을_응답한다() throws Exception {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest("test@test.com", "wrong-password");
        given(loginByEmailUseCase.login(request.toCommand()))
                .willThrow(new LoginException(INVALID_EMAIL_OR_PASSWORD));


        // when
        mockMvc.perform(post("/login/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        then(loginByEmailUseCase).should().login(request.toCommand());
    }
}