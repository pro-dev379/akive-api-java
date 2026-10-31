package net.akive.api.user.adapter.webapi.fixture;

import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserSignUpByEmailRequest.LegalDocumentActionRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserDetailUpdateRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserWithdrawRequest;
import net.akive.api.user.domain.WithdrawalReason;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static net.akive.api.common.domain.EmailFixture.EMAIL_ADDRESS;
import static net.akive.api.legal.domain.fixture.LegalDocumentActionFixture.REQUEST_ID;
import static net.akive.api.legal.domain.fixture.LegalDocumentFixture.LEGAL_DOCUMENT_ID;
import static net.akive.api.user.domain.SignUpLegalDocumentActionType.AGREED;
import static net.akive.api.user.domain.UserPolicy.AIM_MAX_LENGTH;
import static net.akive.api.user.domain.UserPolicy.NICKNAME_MAX_LENGTH;
import static net.akive.api.user.domain.fixture.UserDetailFixture.*;
import static net.akive.api.user.domain.fixture.UserFixture.PASSWORD;
import static net.akive.api.user.domain.fixture.WithdrawalFixture.WITHDRAWAL_REASON;

public class MemberAccountRequestFixture {

    private MemberAccountRequestFixture() {
    }

    public static UserSignUpByEmailRequest createUserSignUpByEmailRequest() {
        return createUserSignUpByEmailRequest(
                EMAIL_ADDRESS,
                PASSWORD,
                NICKNAME,
                createLegalDocumentActionRequests()
        );
    }

    public static UserSignUpByEmailRequest createUserSignUpByEmailRequest(
            String email,
            String password,
            String nickname,
            List<LegalDocumentActionRequest> legalDocumentActions
    ) {
        return new UserSignUpByEmailRequest(email, password, nickname, legalDocumentActions);
    }

    public static Stream<UserSignUpByEmailRequest> invalidUserSignUpByEmailRequests() {
        return Stream.of(
                createUserSignUpByEmailRequest(null, PASSWORD, NICKNAME, createLegalDocumentActionRequests()),
                createUserSignUpByEmailRequest("", PASSWORD, NICKNAME, createLegalDocumentActionRequests()),
                createUserSignUpByEmailRequest(" ", PASSWORD, NICKNAME, createLegalDocumentActionRequests()),
                createUserSignUpByEmailRequest(
                        "invalid-email",
                        PASSWORD,
                        NICKNAME,
                        createLegalDocumentActionRequests()
                ),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        null,
                        NICKNAME,
                        createLegalDocumentActionRequests()
                ),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        "short1!",
                        NICKNAME,
                        createLegalDocumentActionRequests()
                ),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        "password",
                        NICKNAME,
                        createLegalDocumentActionRequests()
                ),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        PASSWORD,
                        "123456789012345678901",
                        createLegalDocumentActionRequests()
                ),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, PASSWORD, NICKNAME, null),
                createUserSignUpByEmailRequest(EMAIL_ADDRESS, PASSWORD, NICKNAME, List.of()),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        PASSWORD,
                        NICKNAME,
                        Collections.singletonList(null)
                ),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        PASSWORD,
                        NICKNAME,
                        List.of(new LegalDocumentActionRequest(null, LEGAL_DOCUMENT_ID, AGREED))
                ),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        PASSWORD,
                        NICKNAME,
                        List.of(new LegalDocumentActionRequest(REQUEST_ID, 0L, AGREED))
                ),
                createUserSignUpByEmailRequest(
                        EMAIL_ADDRESS,
                        PASSWORD,
                        NICKNAME,
                        List.of(new LegalDocumentActionRequest(REQUEST_ID, LEGAL_DOCUMENT_ID, null))
                )
        );
    }

    private static List<LegalDocumentActionRequest> createLegalDocumentActionRequests() {
        return List.of(new LegalDocumentActionRequest(REQUEST_ID, LEGAL_DOCUMENT_ID, AGREED));
    }

    public static UserDetailUpdateRequest createUserDetailUpdateRequest() {
        return createUserDetailUpdateRequest(NICKNAME, PROFILE_IMAGE_ASSET_ID, AIM);
    }

    public static UserDetailUpdateRequest createUserDetailUpdateRequest(UUID profileImageAssetId) {
        return createUserDetailUpdateRequest(NICKNAME, profileImageAssetId, AIM);
    }

    public static UserDetailUpdateRequest createUserDetailUpdateRequest(
            String nickname,
            UUID profileImageAssetId,
            String aim
    ) {
        UserDetailUpdateRequest.UserDetailRequest targetUserDetail =
                new UserDetailUpdateRequest.UserDetailRequest(nickname, aim, profileImageAssetId);

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

    public static UserWithdrawRequest createUserWithdrawRequest(WithdrawalReason withdrawReason) {
        return new UserWithdrawRequest(withdrawReason);
    }
}
