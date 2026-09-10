package net.akive.api.user.adapter.webapi;

import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.adapter.webapi.dto.request.UserDetailUpdateRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserWithdrawRequest;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.dto.UserDetailUpdateCommand;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.util.stream.Stream;

import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.user.domain.UserPolicy.*;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWAL_REASON;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(MemberAccountPrivateApi.class)
class MemberAccountPrivateApiTest extends PrivateApiTestSupport {

    private static final String MEMBER_ME_URL = "/members/me";
    private static final String MEMBER_WITHDRAW_URL = "/members/me/withdraw";

    @MockitoBean
    UserAccountManager userAccountManager;

    @MockitoBean
    UserFinder userFinder;

    @Test
    void 개인_정보를_조회한다() {
        User user = createReconstructedUser(USER_ID);
        given(userFinder.findOrThrowByUserIdAndNotWithdrawn(USER_ID)).willReturn(user);

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.email").isEqualTo(EMAIL.value());
        result.bodyJson().extractingPath("$.roleType").isEqualTo(ROLE_TYPE.name());
        result.bodyJson().extractingPath("$.loginType").isEqualTo(LOGIN_TYPE.name());
        result.bodyJson().extractingPath("$.userDetail.nickname").isEqualTo(NICKNAME);
        result.bodyJson().extractingPath("$.userDetail.aim").isNull();
        result.bodyJson().extractingPath("$.userDetail.imgUrl").isNull();

        then(userFinder).should().findOrThrowByUserIdAndNotWithdrawn(USER_ID);
    }

    @Test
    void 개인_정보_조회_시_멤버를_조회할_수_없으면_404를_응답한다() {
        given(userFinder.findOrThrowByUserIdAndNotWithdrawn(UNKNOWN_USER_ID))
                .willThrow(new ResourceNotFoundException(USER_NOT_FOUND));

        MvcTestResultAssert result = get(MEMBER_ME_URL, UNKNOWN_USER_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(userFinder).should().findOrThrowByUserIdAndNotWithdrawn(UNKNOWN_USER_ID);
    }

    @Test
    void 개인_상세_정보를_수정한다() {
        UserDetailUpdateRequest request = createUserDetailUpdateRequest();
        UserDetailUpdateCommand command = new UserDetailUpdateCommand(USER_ID, NICKNAME, IMG_URL, AIM);

        patchJson(MEMBER_ME_URL, USER_ID, toJson(request)).hasStatus(NO_CONTENT);

        then(userAccountManager).should().updateUserDetail(command);
    }

    @ParameterizedTest
    @MethodSource("invalidUserDetailUpdateRequests")
    void 개인_상세_정보_수정_요청값이_올바르지_않으면_400을_응답한다(UserDetailUpdateRequest request) {
        patchJson(MEMBER_ME_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(userAccountManager).should(never()).updateUserDetail(any(UserDetailUpdateCommand.class));
    }

    @Test
    void 개인_상세_정보_수정_시_멤버를_조회할_수_없으면_404를_응답한다() {
        UserDetailUpdateRequest request = createUserDetailUpdateRequest();
        UserDetailUpdateCommand command = new UserDetailUpdateCommand(UNKNOWN_USER_ID, NICKNAME, IMG_URL, AIM);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(userAccountManager)
                .updateUserDetail(command);

        MvcTestResultAssert result = patchJson(MEMBER_ME_URL, UNKNOWN_USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(userAccountManager).should().updateUserDetail(command);
    }

    @Test
    void 개인_상세_정보_수정_시_이미_존재하는_닉네임이면_409를_응답한다() {
        UserDetailUpdateRequest request = createUserDetailUpdateRequest();
        UserDetailUpdateCommand command = new UserDetailUpdateCommand(USER_ID, NICKNAME, IMG_URL, AIM);
        willThrow(new ResourceConflictException(NICKNAME_ALREADY_EXISTS))
                .given(userAccountManager)
                .updateUserDetail(command);

        MvcTestResultAssert result = patchJson(MEMBER_ME_URL, USER_ID, toJson(request))
                .hasStatus(CONFLICT);

        assertErrorResponse(result, NICKNAME_ALREADY_EXISTS);
        then(userAccountManager).should().updateUserDetail(command);
    }

    @Test
    void 회원을_탈퇴시킨다() {
        UserWithdrawRequest request = new UserWithdrawRequest(WITHDRAWAL_REASON);
        UserWithdrawCommand command = UserWithdrawCommand.of(USER_ID, request.withdrawReason());

        postJson(MEMBER_WITHDRAW_URL, USER_ID, toJson(request)).hasStatus(NO_CONTENT);

        then(userAccountManager).should().withdraw(command);
    }

    @Test
    void 회원_탈퇴_요청값이_올바르지_않으면_400을_응답한다() {
        UserWithdrawRequest request = new UserWithdrawRequest("a".repeat(WITHDRAWAL_REASON_MAX_LENGTH + 1));

        postJson(MEMBER_WITHDRAW_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(userAccountManager).should(never()).withdraw(any(UserWithdrawCommand.class));
    }

    @Test
    void 회원_탈퇴_시_멤버를_조회할_수_없으면_404를_응답한다() {
        UserWithdrawRequest request = new UserWithdrawRequest(WITHDRAWAL_REASON);
        UserWithdrawCommand command = UserWithdrawCommand.of(UNKNOWN_USER_ID, request.withdrawReason());
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(userAccountManager)
                .withdraw(command);

        MvcTestResultAssert result = postJson(MEMBER_WITHDRAW_URL, UNKNOWN_USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(userAccountManager).should().withdraw(command);
    }

    private static UserDetailUpdateRequest createUserDetailUpdateRequest() {
        return createUserDetailUpdateRequest(NICKNAME, IMG_URL, AIM);
    }

    private static UserDetailUpdateRequest createUserDetailUpdateRequest(
            String nickname,
            String imgUrl,
            String aim
    ) {
        UserDetailUpdateRequest.UserDetailRequest targetUserDetail =
                new UserDetailUpdateRequest.UserDetailRequest(nickname, imgUrl, aim);

        return new UserDetailUpdateRequest(targetUserDetail);
    }

    private static Stream<Arguments> invalidUserDetailUpdateRequests() {
        return Stream.of(
                arguments(new UserDetailUpdateRequest(null)),
                arguments(createUserDetailUpdateRequest(null, IMG_URL, AIM)),
                arguments(createUserDetailUpdateRequest("", IMG_URL, AIM)),
                arguments(createUserDetailUpdateRequest(" ", IMG_URL, AIM)),
                arguments(createUserDetailUpdateRequest("a".repeat(NICKNAME_MAX_LENGTH + 1), IMG_URL, AIM)),
                arguments(createUserDetailUpdateRequest(NICKNAME, IMG_URL, "a".repeat(AIM_MAX_LENGTH + 1)))
        );
    }
}
