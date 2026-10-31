package net.akive.api.user.adapter.webapi;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.core.config.security.RefreshTokenCookieExpirer;
import net.akive.api.image.adapter.webapi.mapper.ImageResponseMapper;
import net.akive.api.image.application.provided.ImageAssetReader;
import net.akive.api.image.domain.ImageAsset;
import net.akive.api.user.adapter.webapi.dto.request.UserDetailUpdateRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserWithdrawRequest;
import net.akive.api.user.adapter.webapi.dto.response.UserFindResponse;
import net.akive.api.user.adapter.webapi.dto.response.UserFindResponse.ProfileImageResponse;
import net.akive.api.user.adapter.webapi.dto.response.UserNicknameCheckResponse;
import net.akive.api.user.adapter.webapi.dto.response.WithdrawalReasonsFindResponse;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.UserNicknameChecker;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static net.akive.api.common.exception.business.ResourceNotFoundError.IMAGE_ASSET_NOT_FOUND;
import static net.akive.api.image.domain.VariantType.DETAIL;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Validated
@RestController
@RequestMapping("/members/me")
@RequiredArgsConstructor
public class MemberPrivateApi {

    private final UserFinder userFinder;
    private final UserNicknameChecker userNicknameChecker;
    private final UserAccountManager userAccountManager;
    private final ImageAssetReader imageAssetReader;

    private final ImageResponseMapper imageResponseMapper;
    private final RefreshTokenCookieExpirer refreshTokenCookieExpirer;

    @GetMapping
    public UserFindResponse find(@AuthenticatedUserId Long userId) {
        User foundUser = userFinder.findOrThrow(userId);

        return UserFindResponse.of(foundUser, findProfileImage(foundUser));
    }

    @PatchMapping
    @ResponseStatus(NO_CONTENT)
    public void updateMyDetail(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody UserDetailUpdateRequest request
    ) {
        userAccountManager.updateUserDetail(request.to(userId));
    }

    @GetMapping("/check-nickname")
    public UserNicknameCheckResponse checkNickname(
            @AuthenticatedUserId Long userId,
            @RequestParam @NotBlank String targetNickname
    ) {
        boolean isDuplicated = userNicknameChecker.existsByNicknameExcludingUser(targetNickname, userId);

        return new UserNicknameCheckResponse(isDuplicated);
    }

    @GetMapping("/withdrawal-reasons")
    public WithdrawalReasonsFindResponse findWithdrawalReasons() {
        return WithdrawalReasonsFindResponse.from(userFinder.findWithdrawalReasons());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody UserWithdrawRequest request
    ) {
        UserWithdrawCommand command = UserWithdrawCommand.of(userId, request.withdrawReason());

        userAccountManager.withdraw(command);

        return ResponseEntity.noContent()
                .header(SET_COOKIE, refreshTokenCookieExpirer.expire().toString())
                .build();
    }

    private ProfileImageResponse findProfileImage(User user) {
        UUID imageAssetId = user.getUserDetail().profileImageAssetId();
        if (imageAssetId == null) {
            return null;
        }

        ImageAsset imageAsset = imageAssetReader.find(imageAssetId)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_ASSET_NOT_FOUND));
        URI imageUrl = imageResponseMapper.resolveUrl(imageAsset, DETAIL).orElse(null);

        return ProfileImageResponse.of(
                imageAsset.getImageAssetId(),
                imageAsset.getProcessingStatus(),
                imageUrl
        );
    }
}
