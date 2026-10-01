package net.akive.api.user.adapter.webapi;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.file.application.provided.ImageUrlFinder;
import net.akive.api.user.adapter.webapi.dto.request.UserDetailUpdateRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserWithdrawRequest;
import net.akive.api.user.adapter.webapi.dto.response.UserFindResponse;
import net.akive.api.user.adapter.webapi.dto.response.UserFindResponse.ProfileImageResponse;
import net.akive.api.user.adapter.webapi.dto.response.UserNicknameCheckResponse;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.UserNicknameChecker;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.domain.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static net.akive.api.file.domain.VariantType.DETAIL;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Validated
@RestController
@RequestMapping("/members/me")
@RequiredArgsConstructor
public class MemberPrivateApi {

    private final UserFinder userFinder;
    private final UserNicknameChecker userNicknameChecker;
    private final UserAccountManager userAccountManager;
    private final ImageUrlFinder imageUrlFinder;

    @GetMapping
    public UserFindResponse find(@AuthenticatedUserId Long userId) {
        User foundUser = userFinder.findOrThrowByUserIdAndNotWithdrawn(userId);

        return UserFindResponse.from(foundUser, findProfileImage(foundUser));
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

    @PostMapping("/withdraw")
    @ResponseStatus(NO_CONTENT)
    public void withdraw(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody UserWithdrawRequest request
    ) {
        UserWithdrawCommand command = UserWithdrawCommand.of(userId, request.withdrawReason());

        userAccountManager.withdraw(command);
    }

    private ProfileImageResponse findProfileImage(User user) {
        UUID imageAssetId = user.getUserDetail().profileImageAssetId();
        if (imageAssetId == null) {
            return null;
        }

        return new ProfileImageResponse(
                imageAssetId,
                imageUrlFinder.findImageUrl(imageAssetId, DETAIL).orElse(null)
        );
    }
}
