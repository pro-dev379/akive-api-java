package net.akive.api.user.adapter.webapi;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.support.PublicApiTestSupport;
import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(MemberAccountPublicApi.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberAccountPublicApiTest extends PublicApiTestSupport {

    private static final String SIGN_UP_MEMBER_BY_EMAIL_URL = "/members/email";

    @MockitoBean
    UserAccountManager userAccountManager;

    @MockitoBean
    UserFinder userFinder;

    @Test
    void 이메일로_멤버_가입을_요청하면_201을_응답한다() throws Exception {
        UserSignUpByEmailRequest request = createRequest();
        UserSignUpByEmailCommand command = UserSignUpByEmailCommand.of(
                request.email(),
                request.password(),
                request.nickname()
        );

        postJson(SIGN_UP_MEMBER_BY_EMAIL_URL, request).hasStatus(CREATED);

        then(userAccountManager).should().signUpMemberByEmail(command);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void 이메일_멤버_가입_요청값이_올바르지_않으면_400을_응답한다(UserSignUpByEmailRequest request) throws Exception {
        postJson(SIGN_UP_MEMBER_BY_EMAIL_URL, request).hasStatus(BAD_REQUEST);

        then(userAccountManager).should(never()).signUpMemberByEmail(any(UserSignUpByEmailCommand.class));
    }

    @Test
    void 이메일_멤버_가입_요청_시_이미_가입된_회원이라면_409를_응답한다() throws Exception {
        UserSignUpByEmailRequest request = createRequest();
        UserSignUpByEmailCommand command = UserSignUpByEmailCommand.of(
                request.email(),
                request.password(),
                request.nickname()
        );
        willThrow(new ResourceConflictException(EMAIL_ALREADY_EXISTS))
                .given(userAccountManager)
                .signUpMemberByEmail(command);

        MvcTestResultAssert result = postJson(SIGN_UP_MEMBER_BY_EMAIL_URL, request).hasStatus(CONFLICT);

        assertErrorResponse(result, EMAIL_ALREADY_EXISTS);
    }

    @Test
    void 이메일_멤버_가입_요청_시_닉네임이_중복되면_409를_응답한다() throws Exception {
        UserSignUpByEmailRequest request = createRequest();
        UserSignUpByEmailCommand command = UserSignUpByEmailCommand.of(
                request.email(),
                request.password(),
                request.nickname()
        );
        willThrow(new ResourceConflictException(NICKNAME_ALREADY_EXISTS))
                .given(userAccountManager)
                .signUpMemberByEmail(command);

        MvcTestResultAssert result = postJson(SIGN_UP_MEMBER_BY_EMAIL_URL, request).hasStatus(CONFLICT);

        assertErrorResponse(result, NICKNAME_ALREADY_EXISTS);
    }

    private static Stream<UserSignUpByEmailRequest> invalidRequests() {
        return Stream.of(
                createRequest(null, PASSWORD, NICKNAME),
                createRequest("", PASSWORD, NICKNAME),
                createRequest(" ", PASSWORD, NICKNAME),
                createRequest("invalid-email", PASSWORD, NICKNAME),
                createRequest("test@", PASSWORD, NICKNAME),
                createRequest("@test.com", PASSWORD, NICKNAME),
                createRequest(EMAIL_ADDRESS, null, NICKNAME),
                createRequest(EMAIL_ADDRESS, "", NICKNAME),
                createRequest(EMAIL_ADDRESS, " ", NICKNAME),
                createRequest(EMAIL_ADDRESS, "short1!", NICKNAME),
                createRequest(EMAIL_ADDRESS, "password", NICKNAME),
                createRequest(EMAIL_ADDRESS, PASSWORD, "123456789012345678901")
        );
    }

    private static UserSignUpByEmailRequest createRequest() {
        return createRequest(EMAIL_ADDRESS, PASSWORD, NICKNAME);
    }

    private static UserSignUpByEmailRequest createRequest(String email, String password, String nickname) {
        return new UserSignUpByEmailRequest(email, password, nickname);
    }
}
