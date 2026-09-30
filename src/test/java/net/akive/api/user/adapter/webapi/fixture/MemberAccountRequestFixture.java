package net.akive.api.user.adapter.webapi.fixture;

import net.akive.api.user.adapter.webapi.dto.request.UserDetailUpdateRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserWithdrawRequest;

import java.util.UUID;
import java.util.stream.Stream;

import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.user.domain.UserPolicy.AIM_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWAL_REASON;

public class MemberAccountRequestFixture {

    private MemberAccountRequestFixture() {
    }

    public static UserSignUpByEmailRequest createUserSignUpByEmailRequest() {
        return createUserSignUpByEmailRequest(EMAIL_ADDRESS, PASSWORD, NICKNAME);
    }

    public static UserSignUpByEmailRequest createUserSignUpByEmailRequest(
            String email,
            String password,
            String nickname
    ) {
        return new UserSignUpByEmailRequest(email, password, nickname);
    }

    public static Stream<UserSignUpByEmailRequest> invalidSignUpRequests() {
        return Stream.of(
                createUserSignUpByEmailRequest(null, PASSWORD, NICKNAME),
                createUserSignUpByEmailRequest("", PASSWORD, NICKNAME),
                createUserSignUpByEmailRequest(" ", PASSWORD, NICKNAME),
                createUserSignUpByEmailRequest("invalid-email", PASSWORD, NICKNAME),
                createUserSignUpByEmailRequest("test@", PASSWORD, NICKNAME),
                createUserSignUpByEmailRequest("@test.com", PASSWORD, NICKNAME),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, null, NICKNAME),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, "", NICKNAME),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, " ", NICKNAME),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, "short1!", NICKNAME),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, "password", NICKNAME),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, PASSWORD, "123456789012345678901")
        );
    }

    public static UserDetailUpdateRequest createUserDetailUpdateRequest() {
        return createUserDetailUpdateRequest(NICKNAME, PROFILE_IMAGE_ASSET_ID, AIM);
    }

    public static UserDetailUpdateRequest createUserDetailUpdateRequest(
            String nickname,
            UUID profileImageAssetId,
            String aim
    ) {
        UserDetailUpdateRequest.UserDetailRequest targetUserDetail =
                new UserDetailUpdateRequest.UserDetailRequest(nickname, profileImageAssetId, aim);

        return new UserDetailUpdateRequest(targetUserDetail);
    }

    public static Stream<UserDetailUpdateRequest> invalidDetailUpdateRequests() {
        return Stream.of(
                new UserDetailUpdateRequest(null),
                createUserDetailUpdateRequest(null, PROFILE_IMAGE_ASSET_ID, AIM),
                createUserDetailUpdateRequest("", PROFILE_IMAGE_ASSET_ID, AIM),
                createUserDetailUpdateRequest(" ", PROFILE_IMAGE_ASSET_ID, AIM),
                createUserDetailUpdateRequest(
                        "a".repeat(NICKNAME_MAX_LENGTH + 1),
                        PROFILE_IMAGE_ASSET_ID,
                        AIM
                ),
                createUserDetailUpdateRequest(
                        NICKNAME,
                        PROFILE_IMAGE_ASSET_ID,
                        "a".repeat(AIM_MAX_LENGTH + 1)
                )
        );
    }

    public static UserWithdrawRequest createUserWithdrawRequest() {
        return createUserWithdrawRequest(WITHDRAWAL_REASON);
    }

    public static UserWithdrawRequest createUserWithdrawRequest(String withdrawReason) {
        return new UserWithdrawRequest(withdrawReason);
    }
}
