package net.akive.api.auth.presentation;

import net.akive.api.auth.application.dto.command.emailverification.SendEmailVerificationCommand;
import net.akive.api.auth.application.dto.result.emailverification.SendEmailVerificationResult;
import net.akive.api.auth.application.dto.command.emailverification.VerifyEmailVerificationCodeCommand;
import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.common.exception.business.TooManyRequestsException;
import net.akive.api.common.exception.business.UnauthorizedException;
import net.akive.api.auth.presentation.dto.request.emailverification.SendEmailVerificationCodeRequest;
import net.akive.api.auth.presentation.dto.request.emailverification.VerifyEmailVerificationCodeRequest;
import net.akive.api.support.PublicControllerTestSupport;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.*;
import static net.akive.api.common.exception.business.BadRequestError.*;
import static net.akive.api.common.exception.business.ConflictError.*;
import static net.akive.api.common.exception.business.NotFoundError.*;
import static net.akive.api.common.exception.business.TooManyRequestsError.*;
import static net.akive.api.common.exception.business.UnauthorizedError.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EmailVerificationControllerTest extends PublicControllerTestSupport {

    @Test
    void 인증번호_전송을_요청하면_인증정보를_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("test@test.com", SIGN_UP);
        SendEmailVerificationResult result = SendEmailVerificationResult.of(
                "test@test.com",
                LocalDateTime.of(2025, 5, 21, 10, 5),
                300,
                30
        );

        given(sendEmailVerificationCodeUseCase.send(request.to()))
                .willReturn(result);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.expireSeconds").value(300))
                .andExpect(jsonPath("$.resendIntervalSeconds").value(30));

        then(sendEmailVerificationCodeUseCase)
                .should()
                .send(SendEmailVerificationCommand.of("test@test.com", SIGN_UP));
    }

    @Test
    void 인증번호_전송_요청_시_이메일_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("invalid-email", SIGN_UP);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailVerificationCodeUseCase)
                .should(never())
                .send(any(SendEmailVerificationCommand.class));
    }

    @Test
    void 인증번호_전송_요청_시_이메일이_비어있으면_400을_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("", SIGN_UP);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailVerificationCodeUseCase)
                .should(never())
                .send(any(SendEmailVerificationCommand.class));
    }

    @Test
    void 인증번호_전송_요청_시_인증목적이_없으면_400을_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("test@test.com", null);

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(sendEmailVerificationCodeUseCase)
                .should(never())
                .send(any(SendEmailVerificationCommand.class));
    }

    @Test
    void 인증번호_전송_요청_시_재전송_횟수를_초과하면_429를_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("test@test.com", SIGN_UP);

        willThrow(new TooManyRequestsException(EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED))
                .given(sendEmailVerificationCodeUseCase)
                .send(request.to());

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_429_EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Email verification resend limit exceeded."));
    }

    @Test
    void 인증번호_전송_요청_시_재전송_가능시간_이전이면_429를_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("test@test.com", SIGN_UP);

        willThrow(new TooManyRequestsException(EMAIL_VERIFICATION_RESEND_TOO_FREQUENT))
                .given(sendEmailVerificationCodeUseCase)
                .send(request.to());

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_429_EMAIL_VERIFICATION_RESEND_TOO_FREQUENT"))
                .andExpect(jsonPath("$.message").value("Email verification resend is too frequent."));
    }

    @Test
    void 인증번호_전송_요청_시_패스워드를_찾을_회원이_없으면_404를_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("test@test.com", FIND_PASSWORD);

        willThrow(new NotFoundException(USER_NOT_FOUND))
                .given(sendEmailVerificationCodeUseCase)
                .send(any(SendEmailVerificationCommand.class));

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 인증번호_전송_요청_시_회원가입_이메일이_중복되면_409를_응답한다() throws Exception {
        // given
        SendEmailVerificationCodeRequest request = new SendEmailVerificationCodeRequest("test@test.com", SIGN_UP);

        willThrow(new ConflictException(EMAIL_ALREADY_EXISTS))
                .given(sendEmailVerificationCodeUseCase)
                .send(any(SendEmailVerificationCommand.class));

        // when & then
        mockMvc.perform(post("/emails/verification-codes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 올바른_인증번호로_검증을_요청하면_204를_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                SIGN_UP,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        then(verifyEmailVerificationCodeUseCase)
                .should()
                .verify(
                    VerifyEmailVerificationCodeCommand.of(
                            "test@test.com",
                            SIGN_UP,
                            "123456")
                );
    }

    @Test
    void 인증번호_검증_요청_시_이메일이_비어있으면_400을_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "",
                SIGN_UP,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(VerifyEmailVerificationCodeCommand.class));
    }

    @Test
    void 인증번호_검증_요청_시_이메일_형식이_올바르지_않으면_400을_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "invalid-email",
                SIGN_UP,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(VerifyEmailVerificationCodeCommand.class));
    }

    @Test
    void 인증번호_검증_요청_시_인증번호가_비어있으면_400을_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                SIGN_UP,
                ""
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(VerifyEmailVerificationCodeCommand.class));
    }

    @Test
    void 인증번호_검증_요청_시_인증번호가_6자리_숫자가_아니면_400을_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                SIGN_UP,
                "12345"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(VerifyEmailVerificationCodeCommand.class));
    }

    @Test
    void 인증번호_검증_요청_시_인증목적이_없으면_400을_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                null,
                "123456"
        );

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        then(verifyEmailVerificationCodeUseCase)
                .should(never())
                .verify(any(VerifyEmailVerificationCodeCommand.class));
    }

    @Test
    void 인증번호_검증_요청_시_인증번호가_일치하지_않으면_400을_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                SIGN_UP,
                "000000"
        );

        willThrow(new BadRequestException(EMAIL_VERIFICATION_CODE_MISMATCH))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.to());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_400_EMAIL_VERIFICATION_CODE_MISMATCH"))
                .andExpect(jsonPath("$.message").value("Email verification code mismatch."));
    }

    @Test
    void 인증번호_검증_요청_시_인증정보가_없으면_404를_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                SIGN_UP,
                "123456"
        );

        willThrow(new NotFoundException(EMAIL_VERIFICATION_NOT_FOUND))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.to());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_404_EMAIL_VERIFICATION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Email verification not found."));
    }

    @Test
    void 인증번호_검증_요청_시_인증번호가_만료되면_401을_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                SIGN_UP,
                "123456"
        );

        willThrow(new UnauthorizedException(EMAIL_VERIFICATION_CODE_EXPIRED))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.to());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_401_EMAIL_VERIFICATION_CODE_EXPIRED"))
                .andExpect(jsonPath("$.message").value("Email verification code expired."));
    }

    @Test
    void 인증번호_검증_요청_시_이미_인증된_경우_409를_응답한다() throws Exception {
        // given
        VerifyEmailVerificationCodeRequest request = new VerifyEmailVerificationCodeRequest(
                "test@test.com",
                SIGN_UP,
                "123456"
        );

        willThrow(new ConflictException(EMAIL_ALREADY_VERIFIED))
                .given(verifyEmailVerificationCodeUseCase)
                .verify(request.to());

        // when & then
        mockMvc.perform(post("/emails/verification-codes/verifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_409_EMAIL_ALREADY_VERIFIED"))
                .andExpect(jsonPath("$.message").value("Email is already verified."));
    }
}
