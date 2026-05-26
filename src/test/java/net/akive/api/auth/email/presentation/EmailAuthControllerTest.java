package net.akive.api.auth.email.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailAuthCodeUseCase;
import net.akive.api.auth.email.application.port.in.VerifyEmailAuthCodeUseCase;
import net.akive.api.auth.email.domain.EmailAuthPurpose;
import net.akive.api.auth.email.domain.exception.*;
import net.akive.api.auth.email.presentation.dto.request.EmailAuthCodeSendRequest;
import net.akive.api.auth.email.presentation.dto.request.EmailAuthCodeVerifyRequest;
import net.akive.api.user.excpetion.EmailAlreadyExistsException;
import net.akive.api.user.excpetion.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailAuthController.class)
class EmailAuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    SendEmailAuthCodeUseCase sendEmailAuthCodeUseCase;

    @MockitoBean
    VerifyEmailAuthCodeUseCase verifyEmailAuthCodeUseCase;

    @Test
    void 인증번호_전송을_요청하면_인증정보를_응답한다() throws Exception {
        // given
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest("test@test.com", EmailAuthPurpose.JOIN);
        EmailAuthCodeSendResult result = EmailAuthCodeSendResult.of(
                "test@test.com",
                LocalDateTime.of(2025, 5, 21, 10, 5),
                300,
                30
        );

        given(sendEmailAuthCodeUseCase.sendAuthCode(request.email(), request.authPurpose()))
                .willReturn(result);

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.expiredAt").exists())
                .andExpect(jsonPath("$.expireSeconds").value(300))
                .andExpect(jsonPath("$.resendIntervalSeconds").value(30));

        then(sendEmailAuthCodeUseCase)
                .should()
                .sendAuthCode("test@test.com", EmailAuthPurpose.JOIN);
    }

    @Test
    void 인증번호_전송_요청시_메일_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest("invalid-email", EmailAuthPurpose.JOIN);

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailAuthCodeUseCase)
                .should(never())
                .sendAuthCode(any(), any());
    }

    @Test
    void 인증번호_전송_요청시_이메일이_비어있으면_400을_응답한다() throws Exception {
        // given
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest("", EmailAuthPurpose.JOIN);

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailAuthCodeUseCase)
                .should(never())
                .sendAuthCode(any(), any());
    }

    @Test
    void 인증번호_전송_요청시_인증목적이_없으면_400을_응답한다() throws Exception {
        // given
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest("test@test.com", null);

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailAuthCodeUseCase)
                .should(never())
                .sendAuthCode(any(), any());
    }

    @Test
    void 인증번호_전송_요청시_재전송_횟수를_초과하면_429를_응답한다() throws Exception {
        // given
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN
        );

        willThrow(new EmailAuthResendLimitExceededException())
                .given(sendEmailAuthCodeUseCase)
                .sendAuthCode(request.email(), request.authPurpose());

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_AUTH_429_1"))
                .andExpect(jsonPath("$.message").value("Resend limit exceeded."));
    }

    @Test
    void 인증번호_전송_요청시_재전송_가능시간_이전이면_429를_응답한다() throws Exception {
        // given
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN
        );

        willThrow(new EmailAuthResendTooSoonException())
                .given(sendEmailAuthCodeUseCase)
                .sendAuthCode(request.email(), request.authPurpose());

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_AUTH_429_2"))
                .andExpect(jsonPath("$.message").value("Resend request too soon."));
    }

    @Test
    void 인증번호_전송_요청시_비밀번호를_찾을_회원이_없으면_404를_응답한다() throws Exception {
        // given
        EmailAuthPurpose authPurpose = EmailAuthPurpose.FIND_PASSWORD;
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest(
                "test@test.com",
                authPurpose
        );

        willThrow(new UserNotFoundException())
                .given(sendEmailAuthCodeUseCase)
                .sendAuthCode(anyString(), eq(authPurpose));

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 인증번호_전송_요청시_회원가입_이메일이_중복되면_409를_응답한다() throws Exception {
        // given
        EmailAuthPurpose authPurpose = EmailAuthPurpose.JOIN;
        EmailAuthCodeSendRequest request = new EmailAuthCodeSendRequest(
                "test@test.com",
                authPurpose
        );

        willThrow(new EmailAlreadyExistsException())
                .given(sendEmailAuthCodeUseCase)
                .sendAuthCode(anyString(), eq(authPurpose));

        // when & then
        mockMvc.perform(post("/auth/email/codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 올바른_인증번호로_검증을_요청하면_204를_응답한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        then(verifyEmailAuthCodeUseCase)
                .should()
                .verifyAuthCode(
                        "test@test.com",
                        EmailAuthPurpose.JOIN,
                        "123456"
                );
    }

    @Test
    void 인증번호_검증_요청시_이메일이_비어있으면_400을_반환한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "",
                EmailAuthPurpose.JOIN,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailAuthCodeUseCase)
                .should(never())
                .verifyAuthCode(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_인증번호가_6자리_숫자가_아니면_400을_응답한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN,
                "12345"
        );

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailAuthCodeUseCase)
                .should(never())
                .verifyAuthCode(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_인증목적이_없으면_400을_응답한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "test@test.com",
                null,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailAuthCodeUseCase)
                .should(never())
                .verifyAuthCode(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_인증번호가_일치하지_않으면_400을_응답한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN,
                "000000"
        );

        willThrow(new EmailAuthCodeMismatchException())
                .given(verifyEmailAuthCodeUseCase)
                .verifyAuthCode(request.email(), request.authPurpose(), request.authCode());

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMAIL_AUTH_400_1"))
                .andExpect(jsonPath("$.message").value("Authentication code does not match."));
    }

    @Test
    void 인증번호_검증_요청시_인증정보가_없으면_404를_응답한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN,
                "123456"
        );

        willThrow(new EmailAuthNotFoundException())
                .given(verifyEmailAuthCodeUseCase)
                .verifyAuthCode(request.email(), request.authPurpose(), request.authCode());

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EMAIL_AUTH_404_1"))
                .andExpect(jsonPath("$.message").value("Authentication code not found."));
    }

    @Test
    void 인증번호_검증_요청시_인증번호가_만료되면_401을_응답한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN,
                "123456"
        );

        willThrow(new EmailAuthExpiredException())
                .given(verifyEmailAuthCodeUseCase)
                .verifyAuthCode(request.email(), request.authPurpose(), request.authCode());

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("EMAIL_AUTH_401_1"))
                .andExpect(jsonPath("$.message").value("Authentication code expired."));
    }

    @Test
    void 인증번호_검증_요청시_이미_인증된_경우_409를_응답한다() throws Exception {
        // given
        EmailAuthCodeVerifyRequest request = new EmailAuthCodeVerifyRequest(
                "test@test.com",
                EmailAuthPurpose.JOIN,
                "123456"
        );

        willThrow(new EmailAuthAlreadyVerifiedException())
                .given(verifyEmailAuthCodeUseCase)
                .verifyAuthCode(request.email(), request.authPurpose(), request.authCode());

        // when & then
        mockMvc.perform(post("/auth/email/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_AUTH_409_1"))
                .andExpect(jsonPath("$.message").value("Authentication code already verified."));
    }
}