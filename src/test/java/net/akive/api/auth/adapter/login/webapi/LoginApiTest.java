package net.akive.api.auth.adapter.login.webapi;

import net.akive.api.auth.adapter.login.webapi.dto.request.LoginByEmailRequest;
import net.akive.api.auth.application.login.provided.dto.LoginResult;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static net.akive.api.common.exception.business.UnauthorizedError.INVALID_EMAIL_OR_PASSWORD;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginApiTest extends PublicApiTestSupport {

    @Test
    void 이메일_로그인_요청_시_올바른_이메일과_패스워드를_전달하면_토큰을_발급한다() throws Exception {
        // given
        UUID clientInstanceId = UUID.randomUUID();
        LoginByEmailRequest request = new LoginByEmailRequest("test@test.com", "password123!");
        var command = net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand.of(
                clientInstanceId,
                request.email(),
                request.password()
        );
        LoginResult result = new LoginResult("access-token", "refresh-token");

        given(loginByEmailUseCase.login(command)).willReturn(result);

        // when
        mockMvc.perform(post("/login/email")
                        .header("X-Client-Instance-Id", clientInstanceId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        then(loginByEmailUseCase).should().login(command);
    }

    @Test
    void 이메일_로그인_요청_시_이메일이_비어있으면_400을_응답한다() throws Exception {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest("", "password123!");

        // when
        mockMvc.perform(post("/login/email")
                        .header("X-Client-Instance-Id", UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(loginByEmailUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_로그인_요청_시_패스워드가_비어있으면_400을_응답한다() throws Exception {
        // given
        LoginByEmailRequest request = new LoginByEmailRequest("test@test.com", "");

        // when
        mockMvc.perform(post("/login/email")
                        .header("X-Client-Instance-Id", UUID.randomUUID())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(loginByEmailUseCase).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_로그인_요청_시_존재하지_않는_이메일이면_401을_응답한다() throws Exception {
        // given
        UUID clientInstanceId = UUID.randomUUID();
        LoginByEmailRequest request = new LoginByEmailRequest("none@test.com", "password123!");
        var command = net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand.of(
                clientInstanceId,
                request.email(),
                request.password()
        );
        given(loginByEmailUseCase.login(command))
                .willThrow(new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD));

        // when
        mockMvc.perform(post("/login/email")
                        .header("X-Client-Instance-Id", clientInstanceId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        then(loginByEmailUseCase).should().login(command);
    }

    @Test
    void 이메일_로그인_요청_시_패스워드가_일치하지_않으면_401을_응답한다() throws Exception {
        // given
        UUID clientInstanceId = UUID.randomUUID();
        LoginByEmailRequest request = new LoginByEmailRequest("test@test.com", "wrong-password");
        var command = net.akive.api.auth.application.login.provided.dto.LoginByEmailCommand.of(
                clientInstanceId,
                request.email(),
                request.password()
        );
        given(loginByEmailUseCase.login(command))
                .willThrow(new UnauthorizedException(INVALID_EMAIL_OR_PASSWORD));

        // when
        mockMvc.perform(post("/login/email")
                        .header("X-Client-Instance-Id", clientInstanceId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        then(loginByEmailUseCase).should().login(command);
    }
}
