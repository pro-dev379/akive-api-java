package net.akive.api.user.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.support.PublicApiTestSupport;
import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest;
import net.akive.api.user.adapter.webapi.fixture.MemberAccountRequestFixture;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.UserNicknameChecker;
import net.akive.api.user.application.provided.dto.UserSignUpByEmailCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceConflictError.EMAIL_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.user.adapter.webapi.fixture.MemberAccountRequestFixture.createUserSignUpByEmailRequest;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserDetailFixture.NICKNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(MemberPublicApi.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberPublicApiTest extends PublicApiTestSupport {

    private static final String SIGN_UP_MEMBER_BY_EMAIL_URL = "/members/email";
    private static final String MEMBER_CHECK_NICKNAME_URL = "/members/check-nickname";

    @MockitoBean
    UserAccountManager userAccountManager;

    @MockitoBean
    UserFinder userFinder;

    @MockitoBean
    UserNicknameChecker userNicknameChecker;

    @Test
    void 이메일로_멤버_가입을_요청하면_201을_응답한다() throws Exception {
        UserSignUpByEmailRequest request = createUserSignUpByEmailRequest();
        UserSignUpByEmailCommand command = UserSignUpByEmailCommand.of(
                request.email(),
                request.password(),
                request.nickname()
        );

        postJson(SIGN_UP_MEMBER_BY_EMAIL_URL, request).hasStatus(CREATED);

        then(userAccountManager).should().signUpMemberByEmail(command);
    }

    @ParameterizedTest
    @MethodSource("invalidSignUpRequests")
    void 이메일_멤버_가입_요청값이_올바르지_않으면_400을_응답한다(UserSignUpByEmailRequest request) throws Exception {
        postJson(SIGN_UP_MEMBER_BY_EMAIL_URL, request).hasStatus(BAD_REQUEST);

        then(userAccountManager).should(never()).signUpMemberByEmail(any(UserSignUpByEmailCommand.class));
    }

    @Test
    void 이메일_멤버_가입_요청_시_이미_가입된_회원이라면_409를_응답한다() throws Exception {
        UserSignUpByEmailRequest request = createUserSignUpByEmailRequest();
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
        UserSignUpByEmailRequest request = createUserSignUpByEmailRequest();
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

    @Test
    void 닉네임이_중복인지_확인한다() {
        given(userNicknameChecker.existsByNickname(NICKNAME)).willReturn(true);

        MvcTestResultAssert result = mvcTester.get()
                .uri(MEMBER_CHECK_NICKNAME_URL + "?nickname={nickname}", NICKNAME)
                .assertThat()
                .hasStatus(OK);

        result.bodyJson()
                .extractingPath("$.isDuplicated")
                .isEqualTo(true);
        then(userNicknameChecker).should().existsByNickname(NICKNAME);
    }

    @Test
    void 닉네임이_중복이_아니면_isDuplicated_필드_값을_false로_응답한다() {
        given(userNicknameChecker.existsByNickname(NICKNAME)).willReturn(false);

        MvcTestResultAssert result = mvcTester.get()
                .uri(MEMBER_CHECK_NICKNAME_URL + "?nickname={nickname}", NICKNAME)
                .assertThat()
                .hasStatus(OK);

        result.bodyJson()
                .extractingPath("$.isDuplicated")
                .isEqualTo(false);
        then(userNicknameChecker).should().existsByNickname(NICKNAME);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void 닉네임_중복_확인_요청값이_올바르지_않으면_400을_응답한다(String nickname) {
        MvcTestResultAssert result = mvcTester.get()
                .uri(MEMBER_CHECK_NICKNAME_URL + "?nickname={nickname}", nickname)
                .assertThat()
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_INPUT_VALUE);
        result.bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("nickname");
        then(userNicknameChecker).shouldHaveNoInteractions();
    }

    @Test
    void 닉네임_중복_확인_요청값이_최대_길이를_초과하면_400을_응답한다() {
        String nickname = "a".repeat(NICKNAME_MAX_LENGTH + 1);
        given(userNicknameChecker.existsByNickname(nickname))
                .willThrow(new BusinessRuleViolationException(MAX_LENGTH_EXCEEDED));

        MvcTestResultAssert result = mvcTester.get()
                .uri(MEMBER_CHECK_NICKNAME_URL + "?nickname={nickname}", nickname)
                .assertThat()
                .hasStatus(BAD_REQUEST);

        assertErrorResponse(result, MAX_LENGTH_EXCEEDED);
        then(userNicknameChecker).should().existsByNickname(nickname);
    }

    private static Stream<UserSignUpByEmailRequest> invalidSignUpRequests() {
        return MemberAccountRequestFixture.invalidSignUpRequests();
    }
}
