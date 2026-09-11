package net.akive.api.auth.adapter.emailverification.webapi;

import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeIssueRequest;
import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeVerifyRequest;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeIssuer;
import net.akive.api.auth.application.emailverification.provided.EmailVerificationCodeVerifier;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.common.exception.business.*;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.auth.adapter.emailverification.webapi.fixture.EmailVerificationCodeIssueRequestFixture.createIssueRequest;
import static net.akive.api.auth.adapter.emailverification.webapi.fixture.EmailVerificationCodeIssueRequestFixture.invalidIssueRequests;
import static net.akive.api.auth.adapter.emailverification.webapi.fixture.EmailVerificationCodeVerifyRequestFixture.createVerifyRequest;
import static net.akive.api.auth.adapter.emailverification.webapi.fixture.EmailVerificationCodeVerifyRequestFixture.invalidVerifyRequests;
import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.createEmailVerification;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPolicy.VERIFICATION_CODE_EXPIRATION_DURATION;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.PASSWORD_RESET;
import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.common.exception.business.AuthenticationError.EMAIL_VERIFICATION_CODE_EXPIRED;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.EMAIL_VERIFICATION_CODE_MISMATCH;
import static net.akive.api.common.exception.business.RateLimitError.EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT;
import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_VERIFIED;
import static net.akive.api.common.exception.business.ResourceNotFoundError.EMAIL_VERIFICATION_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(EmailVerificationApi.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailVerificationApiTest extends PublicApiTestSupport {

    private static final String VERIFICATION_CODE_ISSUE_URL = "/auth/email-verifications/codes";
    private static final String VERIFICATION_CODE_VERIFY_URL = "/auth/email-verifications";

    @MockitoBean
    EmailVerificationCodeIssuer emailVerificationCodeIssuer;

    @MockitoBean
    EmailVerificationCodeVerifier emailVerificationCodeVerifier;

    @Test
    void 인증번호를_발급한다() throws Exception {
        EmailVerificationCodeIssueRequest request = createIssueRequest(EMAIL_ADDRESS);
        given(emailVerificationCodeIssuer.issue(request.toCommand())).willReturn(createEmailVerification());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatusOk();

        result.bodyJson().extractingPath("$.email").isEqualTo(EMAIL_ADDRESS);
        result.bodyJson().hasPath("$.expiresAt");
        result.bodyJson().extractingPath("$.expirationSeconds")
                .isEqualTo((int) VERIFICATION_CODE_EXPIRATION_DURATION.toSeconds());
        result.bodyJson().extractingPath("$.renewalIntervalSeconds")
                .isEqualTo((int) EMAIL_VERIFICATION_RENEWAL_MIN_INTERVAL.toSeconds());

        then(emailVerificationCodeIssuer).should().issue(request.toCommand());
    }

    @ParameterizedTest
    @MethodSource("invalidCodeIssueRequests")
    void 인증번호_발급_요청값이_올바르지_않으면_400을_응답한다(EmailVerificationCodeIssueRequest request) throws Exception {
        postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(BAD_REQUEST);

        then(emailVerificationCodeIssuer).should(never()).issue(any(EmailVerificationIssueCommand.class));
    }

    @Test
    void 인증번호_발급_요청_시_인증목적_형식이_올바르지_않으면_400을_응답한다() {
        postJson(
                VERIFICATION_CODE_ISSUE_URL,
                """
                {
                    "email": "test@test.com",
                    "verificationPurpose": "INVALID"
                }
                """
        )
                .hasStatus(BAD_REQUEST);

        then(emailVerificationCodeIssuer).should(never()).issue(any(EmailVerificationIssueCommand.class));
    }

    @Test
    void 인증번호_갱신_가능_시간_이전이면_429를_응답한다() throws Exception {
        EmailVerificationCodeIssueRequest request = createIssueRequest();
        willThrow(new RateLimitExceededException(EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT))
                .given(emailVerificationCodeIssuer)
                .issue(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(TOO_MANY_REQUESTS);

        assertErrorResponse(result, EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT);
    }

    @Test
    void 인증번호_발급_요청_시_패스워드를_리셋할_회원이_없으면_404를_응답한다() throws Exception {
        EmailVerificationCodeIssueRequest request = createIssueRequest(PASSWORD_RESET);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(emailVerificationCodeIssuer)
                .issue(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
    }

    @Test
    void 인증번호_발급_요청_시_회원가입_이메일이_중복되면_409를_응답한다() throws Exception {
        EmailVerificationCodeIssueRequest request = createIssueRequest();
        willThrow(new ResourceConflictException(EMAIL_ALREADY_EXISTS))
                .given(emailVerificationCodeIssuer)
                .issue(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(CONFLICT);

        assertErrorResponse(result, EMAIL_ALREADY_EXISTS);
    }

    @Test
    void 올바른_인증번호로_검증을_요청하면_204를_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();

        postJson(VERIFICATION_CODE_VERIFY_URL, request).hasStatus(NO_CONTENT);

        then(emailVerificationCodeVerifier).should().verify(request.toCommand());
    }

    @ParameterizedTest
    @MethodSource("invalidCodeVerifyRequests")
    void 인증번호_검증_요청값이_올바르지_않으면_400을_응답한다(EmailVerificationCodeVerifyRequest request) throws Exception {
        postJson(VERIFICATION_CODE_VERIFY_URL, request).hasStatus(BAD_REQUEST);

        then(emailVerificationCodeVerifier).should(never()).verify(any(EmailVerificationCodeVerifyCommand.class));
    }

    @Test
    void 인증번호_검증_요청_시_인증목적_형식이_올바르지_않으면_400을_응답한다() {
        postJson(
                VERIFICATION_CODE_VERIFY_URL,
                """
                {
                    "email": "test@test.com",
                    "verificationPurpose": "INVALID",
                    "verificationCode": "123456"
                }
                """
        ).hasStatus(BAD_REQUEST);

        then(emailVerificationCodeVerifier).should(never()).verify(any(EmailVerificationCodeVerifyCommand.class));
    }

    @Test
    void 인증번호_검증_요청_시_인증번호가_일치하지_않으면_400을_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest("000000");
        willThrow(new BusinessRuleViolationException(EMAIL_VERIFICATION_CODE_MISMATCH))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, EMAIL_VERIFICATION_CODE_MISMATCH);
    }

    @Test
    void 인증번호_검증_요청_시_인증정보가_없으면_404를_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();
        willThrow(new ResourceNotFoundException(EMAIL_VERIFICATION_NOT_FOUND))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, EMAIL_VERIFICATION_NOT_FOUND);
    }

    @Test
    void 인증번호_검증_요청_시_인증번호가_만료되면_401을_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();
        willThrow(new AuthenticationException(EMAIL_VERIFICATION_CODE_EXPIRED))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(UNAUTHORIZED);

        assertErrorResponse(result, EMAIL_VERIFICATION_CODE_EXPIRED);
    }

    @Test
    void 인증번호_검증_요청_시_이미_인증된_경우_409를_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();
        willThrow(new ResourceConflictException(EMAIL_ALREADY_VERIFIED))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(CONFLICT);

        assertErrorResponse(result, EMAIL_ALREADY_VERIFIED);
    }

    private static Stream<EmailVerificationCodeIssueRequest> invalidCodeIssueRequests() {
        return invalidIssueRequests();
    }

    private static Stream<EmailVerificationCodeVerifyRequest> invalidCodeVerifyRequests() {
        return invalidVerifyRequests();
    }
}
