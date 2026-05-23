package net.akive.api.auth.email.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.email.application.dto.EmailAuthCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailAuthCodeUseCase;
import net.akive.api.auth.email.application.port.in.VerifyEmailAuthCodeUseCase;
import net.akive.api.auth.email.domain.EmailAuthPurpose;
import net.akive.api.auth.email.presentation.dto.request.EmailAuthCodeSendRequest;
import net.akive.api.auth.email.presentation.dto.request.EmailAuthCodeVerifyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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
    void 이메일_인증_번호_전송_요청을_처리한다() throws Exception {
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
    void 이메일_인증_번호_전송_요청_시_메일_형식이_올바르지_않으면_400을_반환한다() throws Exception {
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
    void 이메일_인증_번호_전송_요청_시_이메일이_비어있으면_400을_반환한다() throws Exception {
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
    void 이메일_인증_번호_전송_요청_시_인증_목적이_없으면_400을_반환한다() throws Exception {
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
    void 이메일_인증번호를_검증한다() throws Exception {
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
                .andExpect(status().isOk());

        then(verifyEmailAuthCodeUseCase)
                .should()
                .verifyAuthCode(
                        "test@test.com",
                        EmailAuthPurpose.JOIN,
                        "123456"
                );
    }

    @Test
    void 인증번호_검증_요청_시_이메일이_비어있으면_400을_반환한다() throws Exception {
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
    void 인증번호_검증_요청_시_인증번호가_6자리_숫자가_아니면_400을_반환한다() throws Exception {
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
    void 인증번호_검증_요청_시_인증목적이_없으면_400을_반환한다() throws Exception {
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
}