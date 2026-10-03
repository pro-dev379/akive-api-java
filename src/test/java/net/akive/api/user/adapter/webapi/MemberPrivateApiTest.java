package net.akive.api.user.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.file.application.provided.ImageAssetReader;
import net.akive.api.support.PrivateApiTestSupport;
import net.akive.api.user.adapter.webapi.dto.request.UserDetailUpdateRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserWithdrawRequest;
import net.akive.api.user.adapter.webapi.fixture.MemberAccountRequestFixture;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.UserNicknameChecker;
import net.akive.api.user.application.provided.dto.UserDetailUpdateCommand;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.domain.User;
import net.akive.api.user.domain.UserDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.file.domain.VariantType.DETAIL;
import static net.akive.api.user.adapter.webapi.fixture.MemberAccountRequestFixture.createUserDetailUpdateRequest;
import static net.akive.api.user.adapter.webapi.fixture.MemberAccountRequestFixture.createUserWithdrawRequest;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.WITHDRAWAL_REASON_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(MemberPrivateApi.class)
class MemberPrivateApiTest extends PrivateApiTestSupport {

    private static final String MEMBER_ME_URL = "/members/me";
    private static final String MEMBER_WITHDRAW_URL = "/members/me/withdraw";
    private static final String MEMBER_CHECK_NICKNAME_URL = "/members/me/check-nickname";
    private static final URI PROFILE_IMAGE_URL =
            URI.create("https://dev-cdn.akive.net/variants/profile-images/1/detail.webp");

    @MockitoBean
    UserAccountManager userAccountManager;

    @MockitoBean
    UserFinder userFinder;

    @MockitoBean
    UserNicknameChecker userNicknameChecker;

    @MockitoBean
    ImageAssetReader imageAssetReader;

    @Test
    void 개인_정보를_조회한다() {
        UserDetail userDetail = createUserDetail();
        User user = createReconstructedUser(USER_ID, userDetail);

        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.findImageUrl(userDetail.profileImageAssetId(), DETAIL))
                .willReturn(Optional.of(PROFILE_IMAGE_URL));

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.email").isEqualTo(EMAIL.value());
        result.bodyJson().extractingPath("$.roleType").isEqualTo(ROLE_TYPE.name());
        result.bodyJson().extractingPath("$.loginType").isEqualTo(LOGIN_TYPE.name());
        result.bodyJson().extractingPath("$.createdAt").isEqualTo(CREATED_AT.toString());
        result.bodyJson().extractingPath("$.userDetail.nickname").isEqualTo(NICKNAME);
        result.bodyJson().extractingPath("$.userDetail.aim").isEqualTo(AIM);
        result.bodyJson().extractingPath("$.userDetail.profileImage.assetId")
                .isEqualTo(userDetail.profileImageAssetId().toString());
        result.bodyJson().extractingPath("$.userDetail.profileImage.url")
                .isEqualTo(PROFILE_IMAGE_URL.toString());

        then(userFinder).should().findOrThrow(USER_ID);
        then(imageAssetReader).should().findImageUrl(userDetail.profileImageAssetId(), DETAIL);
    }

    @Test
    void 프로필_이미지가_없는_개인_정보를_조회한다() {
        User user = createReconstructedUser(USER_ID);

        given(userFinder.findOrThrow(USER_ID)).willReturn(user);

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.userDetail.profileImage").isNull();
        then(userFinder).should().findOrThrow(USER_ID);
        then(imageAssetReader).shouldHaveNoInteractions();
    }

    @Test
    void 개인_정보_조회_시_멤버를_조회할_수_없으면_404를_응답한다() {
        given(userFinder.findOrThrow(UNKNOWN_USER_ID)).willThrow(new ResourceNotFoundException(USER_NOT_FOUND));

        MvcTestResultAssert result = get(MEMBER_ME_URL, UNKNOWN_USER_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(userFinder).should().findOrThrow(UNKNOWN_USER_ID);
    }

    @Test
    void 개인_상세_정보를_수정한다() {
        UserDetailUpdateRequest request = createUserDetailUpdateRequest();
        UserDetailUpdateCommand command = new UserDetailUpdateCommand(USER_ID, NICKNAME, AIM, PROFILE_IMAGE_ASSET_ID);

        patchJson(MEMBER_ME_URL, USER_ID, toJson(request)).hasStatus(NO_CONTENT);

        then(userAccountManager).should().updateUserDetail(command);
    }

    @ParameterizedTest
    @MethodSource("invalidDetailUpdateRequests")
    void 개인_상세_정보_수정_요청값이_올바르지_않으면_400을_응답한다(UserDetailUpdateRequest request) {
        patchJson(MEMBER_ME_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(userAccountManager).should(never()).updateUserDetail(any(UserDetailUpdateCommand.class));
    }

    @Test
    void 개인_상세_정보_수정_시_멤버를_조회할_수_없으면_404를_응답한다() {
        UserDetailUpdateRequest request = createUserDetailUpdateRequest();
        UserDetailUpdateCommand command =
                new UserDetailUpdateCommand(UNKNOWN_USER_ID, NICKNAME, AIM, PROFILE_IMAGE_ASSET_ID);
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
        UserDetailUpdateCommand command =
                new UserDetailUpdateCommand(USER_ID, NICKNAME, AIM, PROFILE_IMAGE_ASSET_ID);
        willThrow(new ResourceConflictException(NICKNAME_ALREADY_EXISTS))
                .given(userAccountManager)
                .updateUserDetail(command);

        MvcTestResultAssert result = patchJson(MEMBER_ME_URL, USER_ID, toJson(request))
                .hasStatus(CONFLICT);

        assertErrorResponse(result, NICKNAME_ALREADY_EXISTS);
        then(userAccountManager).should().updateUserDetail(command);
    }

    @Test
    void 닉네임이_중복인지_확인한다() {
        given(userNicknameChecker.existsByNicknameExcludingUser(NICKNAME, USER_ID)).willReturn(true);

        MvcTestResultAssert result = get(
                MEMBER_CHECK_NICKNAME_URL + "?targetNickname={targetNickname}",
                USER_ID,
                NICKNAME
        ).hasStatus(OK);

        result.bodyJson()
                .extractingPath("$.isDuplicated")
                .isEqualTo(true);
        then(userNicknameChecker).should().existsByNicknameExcludingUser(NICKNAME, USER_ID);
    }

    @Test
    void 닉네임이_중복이_아니면_isDuplicated_필드_값을_false로_응답한다() {
        given(userNicknameChecker.existsByNicknameExcludingUser(NICKNAME, USER_ID)).willReturn(false);

        MvcTestResultAssert result = get(
                MEMBER_CHECK_NICKNAME_URL + "?targetNickname={targetNickname}",
                USER_ID,
                NICKNAME
        ).hasStatus(OK);

        result.bodyJson()
                .extractingPath("$.isDuplicated")
                .isEqualTo(false);
        then(userNicknameChecker).should().existsByNicknameExcludingUser(NICKNAME, USER_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void 닉네임_중복_확인_요청값이_올바르지_않으면_400을_응답한다(String targetNickname) {
        MvcTestResultAssert result = get(
                MEMBER_CHECK_NICKNAME_URL + "?targetNickname={targetNickname}",
                USER_ID,
                targetNickname
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, INVALID_INPUT_VALUE);
        result.bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("targetNickname");
        then(userNicknameChecker).shouldHaveNoInteractions();
    }

    @Test
    void 닉네임_중복_확인_요청값이_최대_길이를_초과하면_400을_응답한다() {
        String targetNickname = "a".repeat(NICKNAME_MAX_LENGTH + 1);
        given(userNicknameChecker.existsByNicknameExcludingUser(targetNickname, USER_ID))
                .willThrow(new BusinessRuleViolationException(MAX_LENGTH_EXCEEDED));

        MvcTestResultAssert result = get(
                MEMBER_CHECK_NICKNAME_URL + "?targetNickname={targetNickname}",
                USER_ID,
                targetNickname
        ).hasStatus(BAD_REQUEST);

        assertErrorResponse(result, MAX_LENGTH_EXCEEDED);
        then(userNicknameChecker).should().existsByNicknameExcludingUser(targetNickname, USER_ID);
    }

    @Test
    void 회원을_탈퇴시킨다() {
        UserWithdrawRequest request = createUserWithdrawRequest();
        UserWithdrawCommand command = UserWithdrawCommand.of(USER_ID, request.withdrawReason());

        postJson(MEMBER_WITHDRAW_URL, USER_ID, toJson(request)).hasStatus(NO_CONTENT);

        then(userAccountManager).should().withdraw(command);
    }

    @Test
    void 회원_탈퇴_요청값이_올바르지_않으면_400을_응답한다() {
        UserWithdrawRequest request = createUserWithdrawRequest("a".repeat(WITHDRAWAL_REASON_MAX_LENGTH + 1));

        postJson(MEMBER_WITHDRAW_URL, USER_ID, toJson(request)).hasStatus(BAD_REQUEST);

        then(userAccountManager).should(never()).withdraw(any(UserWithdrawCommand.class));
    }

    @Test
    void 회원_탈퇴_시_멤버를_조회할_수_없으면_404를_응답한다() {
        UserWithdrawRequest request = createUserWithdrawRequest();
        UserWithdrawCommand command = UserWithdrawCommand.of(UNKNOWN_USER_ID, request.withdrawReason());
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(userAccountManager)
                .withdraw(command);

        MvcTestResultAssert result = postJson(MEMBER_WITHDRAW_URL, UNKNOWN_USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(userAccountManager).should().withdraw(command);
    }

    private static Stream<UserDetailUpdateRequest> invalidDetailUpdateRequests() {
        return MemberAccountRequestFixture.invalidDetailUpdateRequests();
    }
}
