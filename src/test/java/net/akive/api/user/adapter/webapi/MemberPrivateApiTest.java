package net.akive.api.user.adapter.webapi;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.common.exception.business.ResourceConflictException;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.core.config.security.RefreshTokenCookieExpirer;
import net.akive.api.image.adapter.webapi.mapper.ImageResponseMapper;
import net.akive.api.image.application.provided.ImageAssetReader;
import net.akive.api.image.domain.ImageAsset;
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
import net.akive.api.user.domain.WithdrawalReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static net.akive.api.auth.adapter.token.cookie.RefreshTokenCookieManager.REFRESH_TOKEN_COOKIE_NAME;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_INPUT_VALUE;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.exception.business.ResourceConflictError.NICKNAME_ALREADY_EXISTS;
import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.USER_NOT_FOUND;
import static net.akive.api.image.domain.ImageType.PROFILE_IMAGE;
import static net.akive.api.image.domain.ProcessingStatus.READY;
import static net.akive.api.image.domain.ProcessingStatus.UPLOADING;
import static net.akive.api.image.domain.VariantType.DETAIL;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.RECIPE_VERSION;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.UNKNOWN_IMAGE_ASSET_ID;
import static net.akive.api.image.domain.fixture.ImageAssetFixture.readyImageAsset;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.FIRST_IMAGE_VARIANT_ID;
import static net.akive.api.image.domain.fixture.ImageVariantFixture.detailImageVariant;
import static net.akive.api.user.adapter.webapi.fixture.MemberAccountRequestFixture.createUserDetailUpdateRequest;
import static net.akive.api.user.adapter.webapi.fixture.MemberAccountRequestFixture.createUserWithdrawRequest;
import static net.akive.api.user.application.fixture.UserAccountCommandFixture.createUserDetailUpdateCommand;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.*;
import static net.akive.api.user.domain.WithdrawalReason.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.*;

@WebMvcTest(MemberPrivateApi.class)
class MemberPrivateApiTest extends PrivateApiTestSupport {

    private static final String MEMBER_ME_URL = "/members/me";
    private static final String MEMBER_WITHDRAW_URL = "/members/me/withdraw";
    private static final String MEMBER_WITHDRAWAL_REASONS_URL = "/members/me/withdrawal-reasons";
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

    @MockitoBean
    ImageResponseMapper imageResponseMapper;

    @MockitoBean
    RefreshTokenCookieExpirer refreshTokenCookieExpirer;

    @Test
    void 개인_정보를_조회한다() {
        UserDetail userDetail = createUserDetail();
        User user = createReconstructedUser(USER_ID, userDetail);
        ImageAsset profileImageAsset = readyImageAsset(
                PROFILE_IMAGE,
                detailImageVariant(FIRST_IMAGE_VARIANT_ID, RECIPE_VERSION)
        );

        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.find(userDetail.profileImageAssetId())).willReturn(Optional.of(profileImageAsset));
        given(imageResponseMapper.resolveUrl(profileImageAsset, DETAIL))
                .willReturn(Optional.of(PROFILE_IMAGE_URL));

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.email").isEqualTo(EMAIL.value());
        result.bodyJson().extractingPath("$.role").isEqualTo(USER_ROLE.name());
        result.bodyJson().extractingPath("$.loginType").isEqualTo(LOGIN_TYPE.name());
        result.bodyJson().extractingPath("$.createdAt").isEqualTo(CREATED_AT.toString());
        result.bodyJson().extractingPath("$.userDetail.nickname").isEqualTo(NICKNAME);
        result.bodyJson().extractingPath("$.userDetail.aim").isEqualTo(AIM);
        result.bodyJson().extractingPath("$.userDetail.profileImage.assetId")
                .isEqualTo(userDetail.profileImageAssetId().toString());
        result.bodyJson().extractingPath("$.userDetail.profileImage.processingStatus")
                .isEqualTo(READY.name());
        result.bodyJson().extractingPath("$.userDetail.profileImage.url")
                .isEqualTo(PROFILE_IMAGE_URL.toString());

        then(userFinder).should().findOrThrow(USER_ID);
        then(imageAssetReader).should().find(userDetail.profileImageAssetId());
        then(imageResponseMapper).should().resolveUrl(profileImageAsset, DETAIL);
    }

    @Test
    void 프로필_이미지가_없는_개인_정보를_조회한다() {
        User user = createReconstructedUser(USER_ID);

        given(userFinder.findOrThrow(USER_ID)).willReturn(user);

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.userDetail.profileImage").isNull();
        then(userFinder).should().findOrThrow(USER_ID);
        then(imageAssetReader).shouldHaveNoInteractions();
        then(imageResponseMapper).shouldHaveNoInteractions();
    }

    @Test
    void 개인_정보_조회_시_프로필_이미지_에셋은_있지만_URL을_생성할_수_없으면_이미지_리사이즈_처리_상태와_null_URL을_응답한다() {
        UserDetail userDetail = createUserDetail();
        User user = createReconstructedUser(USER_ID, userDetail);
        ImageAsset profileImageAsset = ImageAsset.create(userDetail.profileImageAssetId(), PROFILE_IMAGE);

        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.find(userDetail.profileImageAssetId())).willReturn(Optional.of(profileImageAsset));
        given(imageResponseMapper.resolveUrl(profileImageAsset, DETAIL)).willReturn(Optional.empty());

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.userDetail.profileImage.assetId")
                .isEqualTo(userDetail.profileImageAssetId().toString());
        result.bodyJson().extractingPath("$.userDetail.profileImage.processingStatus")
                .isEqualTo(UPLOADING.name());
        result.bodyJson().extractingPath("$.userDetail.profileImage.url").isNull();
        then(imageAssetReader).should().find(userDetail.profileImageAssetId());
        then(imageResponseMapper).should().resolveUrl(profileImageAsset, DETAIL);
    }

    @Test
    void 개인_정보_조회_시_프로필_이미지_에셋을_찾을_수_없으면_404를_응답한다() {
        UserDetail userDetail = createUserDetail();
        User user = createReconstructedUser(USER_ID, userDetail);

        given(userFinder.findOrThrow(USER_ID)).willReturn(user);
        given(imageAssetReader.find(userDetail.profileImageAssetId())).willReturn(Optional.empty());

        MvcTestResultAssert result = get(MEMBER_ME_URL, USER_ID).hasStatus(NOT_FOUND);

        assertErrorResponse(result, IMAGE_ASSET_NOT_FOUND);
        then(imageAssetReader).should().find(userDetail.profileImageAssetId());
        then(imageResponseMapper).shouldHaveNoInteractions();
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
        UserDetailUpdateCommand command = createUserDetailUpdateCommand();

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
        UserDetailUpdateCommand command = createUserDetailUpdateCommand(UNKNOWN_USER_ID);
        willThrow(new ResourceNotFoundException(USER_NOT_FOUND))
                .given(userAccountManager)
                .updateUserDetail(command);

        MvcTestResultAssert result = patchJson(MEMBER_ME_URL, UNKNOWN_USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, USER_NOT_FOUND);
        then(userAccountManager).should().updateUserDetail(command);
    }

    @Test
    void 개인_상세_정보_수정_시_프로필_이미지_에셋을_조회할_수_없으면_404를_응답한다() {
        UserDetailUpdateRequest request = createUserDetailUpdateRequest(UNKNOWN_IMAGE_ASSET_ID);
        UserDetailUpdateCommand command = createUserDetailUpdateCommand(UNKNOWN_IMAGE_ASSET_ID);
        willThrow(new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND))
                .given(userAccountManager)
                .updateUserDetail(command);

        MvcTestResultAssert result = patchJson(MEMBER_ME_URL, USER_ID, toJson(request))
                .hasStatus(NOT_FOUND);

        assertErrorResponse(result, IMAGE_ASSET_NOT_FOUND);
        then(userAccountManager).should().updateUserDetail(command);
    }

    @Test
    void 개인_상세_정보_수정_시_이미_존재하는_닉네임이면_409를_응답한다() {
        UserDetailUpdateRequest request = createUserDetailUpdateRequest();
        UserDetailUpdateCommand command = createUserDetailUpdateCommand();
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
    void 회원_탈퇴_사유_목록을_조회한다() {
        List<WithdrawalReason> withdrawalReasons = List.of(
                INCONVENIENT_TO_USE,
                MISSING_DESIRED_FEATURES,
                NO_LONGER_NEEDED,
                WANT_NEW_ACCOUNT,
                OTHER
        );
        given(userFinder.findWithdrawalReasons()).willReturn(withdrawalReasons);

        MvcTestResultAssert result = get(MEMBER_WITHDRAWAL_REASONS_URL, USER_ID).hasStatus(OK);

        result.bodyJson().extractingPath("$.withdrawalReasons.length()")
                .isEqualTo(withdrawalReasons.size());
        result.bodyJson().extractingPath("$.withdrawalReasons[0].code")
                .isEqualTo(INCONVENIENT_TO_USE.name());
        result.bodyJson().extractingPath("$.withdrawalReasons[0].label")
                .isEqualTo(INCONVENIENT_TO_USE.getLabel());
        result.bodyJson().extractingPath("$.withdrawalReasons[4].code")
                .isEqualTo(OTHER.name());
        result.bodyJson().extractingPath("$.withdrawalReasons[4].label")
                .isEqualTo(OTHER.getLabel());

        then(userFinder).should().findWithdrawalReasons();
    }

    @Test
    void 회원을_탈퇴시키고_리프레시_토큰_쿠키를_만료시킨다() {
        UserWithdrawRequest request = createUserWithdrawRequest();
        UserWithdrawCommand command = UserWithdrawCommand.of(USER_ID, request.withdrawReason());
        ResponseCookie expiredCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "").build();
        given(refreshTokenCookieExpirer.expire()).willReturn(expiredCookie);

        postJson(MEMBER_WITHDRAW_URL, USER_ID, toJson(request))
                .hasStatus(NO_CONTENT)
                .hasHeader(SET_COOKIE, expiredCookie.toString());

        then(userAccountManager).should().withdraw(command);
        then(refreshTokenCookieExpirer).should().expire();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"withdrawReason\":null}",
            "{\"withdrawReason\":\"UNKNOWN\"}"
    })
    void 회원_탈퇴_요청값이_올바르지_않으면_400을_응답한다(String requestBody) {
        postJson(MEMBER_WITHDRAW_URL, USER_ID, requestBody)
                .hasStatus(BAD_REQUEST);

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
