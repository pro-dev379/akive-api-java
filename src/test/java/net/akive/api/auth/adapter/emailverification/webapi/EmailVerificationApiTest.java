package net.akive.api.auth.adapter.emailverification.webapi;

import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeIssueRequest;
import net.akive.api.auth.adapter.emailverification.webapi.dto.request.EmailVerificationCodeVerifyRequest;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationCodeVerifyCommand;
import net.akive.api.auth.application.emailverification.provided.dto.EmailVerificationIssueCommand;
import net.akive.api.auth.domain.emailverification.EmailVerificationPurpose;
import net.akive.api.common.exception.business.*;
import net.akive.api.support.PublicApiTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.EMAIL_ADDRESS;
import static net.akive.api.auth.domain.emailverification.EmailVerificationFixture.createEmailVerification;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.FIND_PASSWORD;
import static net.akive.api.auth.domain.emailverification.EmailVerificationPurpose.SIGN_UP;
import static net.akive.api.common.exception.business.BadRequestError.EMAIL_VERIFICATION_CODE_MISMATCH;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ConflictError.EMAIL_ALREADY_VERIFIED;
import static net.akive.api.common.exception.business.NotFoundError.EMAIL_VERIFICATION_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.USER_NOT_FOUND;
import static net.akive.api.common.exception.business.TooManyRequestsError.EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT;
import static net.akive.api.common.exception.business.UnauthorizedError.EMAIL_VERIFICATION_CODE_EXPIRED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

class EmailVerificationApiTest extends PublicApiTestSupport {

    private static final String VERIFICATION_CODE = "123456";
    private static final String VERIFICATION_CODE_ISSUE_URL = "/verifications/emails/codes";
    private static final String VERIFICATION_CODE_VERIFY_URL = "/verifications/emails";

    @Test
    void 인증번호를_발급한다() throws Exception {
        EmailVerificationCodeIssueRequest request = createIssueRequest();
        given(emailVerificationCodeIssuer.issue(request.toCommand())).willReturn(createEmailVerification());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatusOk();

        result.bodyJson().extractingPath("$.email").isEqualTo(EMAIL_ADDRESS);
        result.bodyJson().hasPath("$.expiresAt");
        result.bodyJson().extractingPath("$.expirationSeconds").isEqualTo(300);
        result.bodyJson().extractingPath("$.renewalIntervalSeconds").isEqualTo(30);

        then(emailVerificationCodeIssuer).should().issue(request.toCommand());
    }

    @ParameterizedTest
    @MethodSource("invalidIssueRequests")
    void 인증번호_발급_요청값이_올바르지_않으면_400을_응답한다(EmailVerificationCodeIssueRequest request) throws Exception {
        postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(BAD_REQUEST);

        then(emailVerificationCodeIssuer).should(never()).issue(any(EmailVerificationIssueCommand.class));
    }

    @Test
    void 인증번호_발급_요청_시_인증목적_형식이_올바르지_않으면_400을_응답한다() throws Exception {
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
        willThrow(new TooManyRequestsException(EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT))
                .given(emailVerificationCodeIssuer)
                .issue(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(TOO_MANY_REQUESTS);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("EMAIL_VERIFICATION_429_EMAIL_VERIFICATION_RENEWAL_TOO_FREQUENT");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Email verification renewal is too frequent.");
    }

    @Test
    void 인증번호_발급_요청_시_패스워드를_찾을_회원이_없으면_404를_응답한다() throws Exception {
        EmailVerificationCodeIssueRequest request = createIssueRequest(FIND_PASSWORD);
        willThrow(new NotFoundException(USER_NOT_FOUND))
                .given(emailVerificationCodeIssuer)
                .issue(request.toCommand());

        postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(NOT_FOUND);
    }

    @Test
    void 인증번호_발급_요청_시_회원가입_이메일이_중복되면_409를_응답한다() throws Exception {
        EmailVerificationCodeIssueRequest request = createIssueRequest();
        willThrow(new ConflictException(EMAIL_ALREADY_EXISTS))
                .given(emailVerificationCodeIssuer)
                .issue(request.toCommand());

        postJson(VERIFICATION_CODE_ISSUE_URL, request).hasStatus(CONFLICT);
    }

    @Test
    void 올바른_인증번호로_검증을_요청하면_204를_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();

        postJson(VERIFICATION_CODE_VERIFY_URL, request).hasStatus(NO_CONTENT);

        then(emailVerificationCodeVerifier).should().verify(request.toCommand());
    }

    @ParameterizedTest
    @MethodSource("invalidVerifyRequests")
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
        willThrow(new BadRequestException(EMAIL_VERIFICATION_CODE_MISMATCH))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(BAD_REQUEST);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("EMAIL_VERIFICATION_400_EMAIL_VERIFICATION_CODE_MISMATCH");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Email verification code mismatch.");
    }

    @Test
    void 인증번호_검증_요청_시_인증정보가_없으면_404를_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();
        willThrow(new NotFoundException(EMAIL_VERIFICATION_NOT_FOUND))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(NOT_FOUND);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("EMAIL_VERIFICATION_404_EMAIL_VERIFICATION_NOT_FOUND");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Email verification not found.");
    }

    @Test
    void 인증번호_검증_요청_시_인증번호가_만료되면_401을_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();
        willThrow(new UnauthorizedException(EMAIL_VERIFICATION_CODE_EXPIRED))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(UNAUTHORIZED);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("EMAIL_VERIFICATION_401_EMAIL_VERIFICATION_CODE_EXPIRED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Email verification code expired.");
    }

    @Test
    void 인증번호_검증_요청_시_이미_인증된_경우_409를_응답한다() throws Exception {
        EmailVerificationCodeVerifyRequest request = createVerifyRequest();
        willThrow(new ConflictException(EMAIL_ALREADY_VERIFIED))
                .given(emailVerificationCodeVerifier)
                .verify(request.toCommand());

        MvcTestResultAssert result = postJson(VERIFICATION_CODE_VERIFY_URL, request)
                .hasStatus(CONFLICT);

        result.bodyJson()
                .extractingPath("$.code")
                .isEqualTo("EMAIL_VERIFICATION_409_EMAIL_ALREADY_VERIFIED");
        result.bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Email is already verified.");
    }

    private static Stream<EmailVerificationCodeIssueRequest> invalidIssueRequests() {
        return Stream.of(
                createIssueRequest("", SIGN_UP),
                createIssueRequest("invalid-email", SIGN_UP),
                createIssueRequest(EMAIL_ADDRESS, null)
        );
    }

    private static Stream<EmailVerificationCodeVerifyRequest> invalidVerifyRequests() {
        return Stream.of(
                createVerifyRequest("", SIGN_UP, VERIFICATION_CODE),
                createVerifyRequest("invalid-email", SIGN_UP, VERIFICATION_CODE),
                createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, ""),
                createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, "12345"),
                createVerifyRequest(EMAIL_ADDRESS, null, VERIFICATION_CODE)
        );
    }

    private static EmailVerificationCodeIssueRequest createIssueRequest() {
        return createIssueRequest(EMAIL_ADDRESS, SIGN_UP);
    }

    private static EmailVerificationCodeIssueRequest createIssueRequest(EmailVerificationPurpose purpose) {
        return createIssueRequest(EMAIL_ADDRESS, purpose);
    }

    private static EmailVerificationCodeIssueRequest createIssueRequest(
            String email,
            EmailVerificationPurpose purpose
    ) {
        return new EmailVerificationCodeIssueRequest(email, purpose);
    }

    private static EmailVerificationCodeVerifyRequest createVerifyRequest() {
        return createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, VERIFICATION_CODE);
    }

    private static EmailVerificationCodeVerifyRequest createVerifyRequest(String verificationCode) {
        return createVerifyRequest(EMAIL_ADDRESS, SIGN_UP, verificationCode);
    }

    private static EmailVerificationCodeVerifyRequest createVerifyRequest(
            String email,
            EmailVerificationPurpose purpose,
            String verificationCode
    ) {
        return new EmailVerificationCodeVerifyRequest(email, purpose, verificationCode);
    }
}
