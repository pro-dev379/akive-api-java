package net.akive.api.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.domain.emailverification.exception.EmailVerificationException;
import net.akive.api.user.application.dto.SignUpByEmailCommand;
import net.akive.api.user.application.dto.SignUpResult;
import net.akive.api.user.application.port.in.SignUpByEmailUseCase;
import net.akive.api.user.excpetion.UserException;
import net.akive.api.user.presentation.dto.request.SignUpByEmailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static net.akive.api.auth.domain.emailverification.exception.EmailVerificationErrorCode.NOT_VERIFIED;
import static net.akive.api.user.domain.RoleType.USER;
import static net.akive.api.user.excpetion.UserErrorCode.EMAIL_ALREADY_EXISTS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(SignUpController.class)
class SignUpControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    SignUpByEmailUseCase signUpByEmailUseCase;

    @Test
    void 이메일로_회원가입하면_토큰과_닉네임이_응답된다() throws Exception {
        // given
        SignUpByEmailRequest request = new SignUpByEmailRequest(
                "test@test.com",
                USER,
                "password123!"
        );

        SignUpByEmailCommand command = SignUpByEmailCommand.of(
                "test@test.com",
                USER,
                "password123!"
        );

        SignUpResult result = new SignUpResult(
                "USER@abcde",
                "access-token",
                "refresh-token"
        );

        given(signUpByEmailUseCase.signUp(command))
                .willReturn(result);

        // when & then
        mockMvc.perform(post("/users/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("USER@abcde"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        then(signUpByEmailUseCase).should().signUp(command);
    }

    @Test
    void 이메일로_회원가입_요청시_이메일_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        SignUpByEmailRequest request = new SignUpByEmailRequest(
                "invalid-email",
                USER,
                "password123!"
        );

        // when & then
        mockMvc.perform(post("/users/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(signUpByEmailUseCase).should(never()).signUp(any(SignUpByEmailCommand.class));
    }

    @Test
    void 이메일로_회원가입_요청시_회원_타입이_없으면_400을_응답한다() throws Exception {
        // given
        SignUpByEmailRequest request = new SignUpByEmailRequest(
                "test@test.com",
                null,
                "password123!"
        );

        // when & then
        mockMvc.perform(post("/users/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(signUpByEmailUseCase).should(never()).signUp(any(SignUpByEmailCommand.class));
    }

    @Test
    void 이메일로_회원가입_요청시_패스워드_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        SignUpByEmailRequest request = new SignUpByEmailRequest(
                "test@test.com",
                USER,
                "invalid"
        );

        // when & then
        mockMvc.perform(post("/users/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(signUpByEmailUseCase).should(never()).signUp(any(SignUpByEmailCommand.class));
    }

    @Test
    void 이메일로_회원가입_요청시_이메일_인증이_안_된_경우_400을_응답한다() throws Exception {
        // given
        SignUpByEmailRequest request = new SignUpByEmailRequest(
                "test@test.com",
                USER,
                "password123!"
        );

        willThrow(new EmailVerificationException(NOT_VERIFIED))
                .given(signUpByEmailUseCase)
                .signUp(request.toCommand());

        // when & then
        mockMvc.perform(post("/users/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 이메일로_회원가입_요청시_이미_가입된_경우_409를_응답한다() throws Exception{
        // given
        SignUpByEmailRequest request = new SignUpByEmailRequest(
                "test@test.com",
                USER,
                "password123!"
        );

        willThrow(new UserException(EMAIL_ALREADY_EXISTS))
                .given(signUpByEmailUseCase)
                .signUp(request.toCommand());

        // when & then
        mockMvc.perform(post("/users/email")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}