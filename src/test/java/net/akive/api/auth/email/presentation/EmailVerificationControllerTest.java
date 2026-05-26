package net.akive.api.auth.email.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.akive.api.auth.email.application.dto.EmailVerificationCodeSendResult;
import net.akive.api.auth.email.application.port.in.SendEmailVerificationCodeUseCase;
import net.akive.api.auth.email.application.port.in.VerifyEmailVerificationCodeUseCase;
import net.akive.api.auth.email.domain.EmailVerificationPurpose;
import net.akive.api.auth.email.domain.exception.*;
import net.akive.api.auth.email.presentation.dto.request.EmailVerificationCodeSendRequest;
import net.akive.api.auth.email.presentation.dto.request.EmailVerificationCodeVerifyRequest;
import net.akive.api.user.excpetion.UserException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static net.akive.api.auth.email.domain.exception.EmailVerificationErrorCode.*;
import static net.akive.api.user.excpetion.UserErrorCode.*;
import static net.akive.api.user.excpetion.UserErrorCode.NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailVerificationController.class)
class EmailVerificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    SendEmailVerificationCodeUseCase sendEmailVerificationCodeUseCase;

    @MockitoBean
    VerifyEmailVerificationCodeUseCase verifyEmailVerificationCodeUseCase;

    @Test
    void 인증번호_전송을_요청하면_인증정보를_응답한다() throws Exception {
        // given
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest("test@test.com", EmailVerificationPurpose.JOIN);
        EmailVerificationCodeSendResult result = EmailVerificationCodeSendResult.of(
                "test@test.com",
                LocalDateTime.of(2025, 5, 21, 10, 5),
                300,
                30
        );

        given(sendEmailVerificationCodeUseCase.send(request.email(), request.verificationPurpose()))
                .willReturn(result);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.expiredAt").exists())
                .andExpect(jsonPath("$.expireSeconds").value(300))
                .andExpect(jsonPath("$.resendIntervalSeconds").value(30));

        then(sendEmailVerificationCodeUseCase)
                .should()
                .send("test@test.com", EmailVerificationPurpose.JOIN);
    }

    @Test
    void 인증번호_전송_요청시_이메일_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest("invalid-email", EmailVerificationPurpose.JOIN);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailVerificationCodeUseCase)
                .should(never())
                .send(any(), any());
    }

    @Test
    void 인증번호_전송_요청시_이메일이_비어있으면_400을_응답한다() throws Exception {
        // given
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest("", EmailVerificationPurpose.JOIN);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailVerificationCodeUseCase)
                .should(never())
                .send(any(), any());
    }

    @Test
    void 인증번호_전송_요청시_인증목적이_없으면_400을_응답한다() throws Exception {
        // given
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest("test@test.com", null);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailVerificationCodeUseCase)
                .should(never())
                .send(any(), any());
    }

    @Test
    void 인증번호_전송_요청시_재전송_횟수를_초과하면_429를_응답한다() throws Exception {
        // given
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN
        );

        willThrow(new EmailVerificationException(RESEND_LIMIT_EXCEEDED))
                .given(sendEmailVerificationCodeUseCase)
                .send(request.email(), request.verificationPurpose());

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_429_1"))
                .andExpect(jsonPath("$.message").value("Resend limit exceeded."));
    }

    @Test
    void 인증번호_전송_요청시_재전송_가능시간_이전이면_429를_응답한다() throws Exception {
        // given
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN
        );

        willThrow(new EmailVerificationException(RESEND_TOO_SOON))
                .given(sendEmailVerificationCodeUseCase)
                .send(request.email(), request.verificationPurpose());

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_429_2"))
                .andExpect(jsonPath("$.message").value("Resend request too soon."));
    }

    @Test
    void 인증번호_전송_요청시_비밀번호를_찾을_회원이_없으면_404를_응답한다() throws Exception {
        // given
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.FIND_PASSWORD;
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest(
                "test@test.com",
                verificationPurpose
        );

        willThrow(new UserException(NOT_FOUND))
                .given(sendEmailVerificationCodeUseCase)
                .send(anyString(), eq(verificationPurpose));

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 인증번호_전송_요청시_회원가입_이메일이_중복되면_409를_응답한다() throws Exception {
        // given
        EmailVerificationPurpose verificationPurpose = EmailVerificationPurpose.JOIN;
        EmailVerificationCodeSendRequest request = new EmailVerificationCodeSendRequest(
                "test@test.com",
                verificationPurpose
        );

        willThrow(new UserException(EMAIL_ALREADY_EXISTS))
                .given(sendEmailVerificationCodeUseCase)
                .send(anyString(), eq(verificationPurpose));

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 올바른_인증번호로_검증을_요청하면_204를_응답한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        then(verifyEmailVerificationCodeUseCase)
                .should()
                .verify(
                        "test@test.com",
                        EmailVerificationPurpose.JOIN,
                        "123456"
                );
    }

    @Test
    void 인증번호_검증_요청시_이메일이_비어있으면_400을_반환한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "",
                EmailVerificationPurpose.JOIN,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_이메일_형식이_올바르지_않으면_400을_반환한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "invalid-email",
                EmailVerificationPurpose.JOIN,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_인증번호가_비어있으면_400을_반환한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN,
                ""
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_인증번호가_6자리_숫자가_아니면_400을_응답한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN,
                "12345"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_인증목적이_없으면_400을_응답한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                null,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(), any(), any());
    }

    @Test
    void 인증번호_검증_요청시_인증번호가_일치하지_않으면_400을_응답한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN,
                "000000"
        );

        willThrow(new EmailVerificationException(CODE_MISMATCH))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.email(), request.verificationPurpose(), request.verificationCode());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_400_1"))
                .andExpect(jsonPath("$.message").value("Verification code does not match."));
    }

    @Test
    void 인증번호_검증_요청시_인증정보가_없으면_404를_응답한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN,
                "123456"
        );

        willThrow(new EmailVerificationException(EmailVerificationErrorCode.NOT_FOUND))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.email(), request.verificationPurpose(), request.verificationCode());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_404_1"))
                .andExpect(jsonPath("$.message").value("Verification code not found."));
    }

    @Test
    void 인증번호_검증_요청시_인증번호가_만료되면_401을_응답한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN,
                "123456"
        );

        willThrow(new EmailVerificationException(EXPIRED))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.email(), request.verificationPurpose(), request.verificationCode());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_401_1"))
                .andExpect(jsonPath("$.message").value("Verification code expired."));
    }

    @Test
    void 인증번호_검증_요청시_이미_인증된_경우_409를_응답한다() throws Exception {
        // given
        EmailVerificationCodeVerifyRequest request = new EmailVerificationCodeVerifyRequest(
                "test@test.com",
                EmailVerificationPurpose.JOIN,
                "123456"
        );

        willThrow(new EmailVerificationException(ALREADY_VERIFIED))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.email(), request.verificationPurpose(), request.verificationCode());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_409_1"))
                .andExpect(jsonPath("$.message").value("Verification code already verified."));
    }
}