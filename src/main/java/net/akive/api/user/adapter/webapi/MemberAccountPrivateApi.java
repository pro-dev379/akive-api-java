package net.akive.api.user.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.user.adapter.webapi.dto.request.UserDetailUpdateRequest;
import net.akive.api.user.adapter.webapi.dto.request.UserWithdrawRequest;
import net.akive.api.user.adapter.webapi.dto.response.UserFindResponse;
import net.akive.api.user.application.provided.UserAccountManager;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.application.provided.dto.UserWithdrawCommand;
import net.akive.api.user.domain.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/members/me")
@RequiredArgsConstructor
public class MemberAccountPrivateApi {

    private final UserFinder userFinder;
    private final UserAccountManager userAccountManager;

    @GetMapping
    public UserFindResponse find(@AuthenticatedUserId Long userId) {
        User foundUser = userFinder.findOrThrowByUserIdAndNotWithdrawn(userId);

        return UserFindResponse.from(foundUser);
    }

    @PatchMapping
    @ResponseStatus(NO_CONTENT)
    public void updateMyDetail(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody UserDetailUpdateRequest request
    ) {
        userAccountManager.updateUserDetail(request.to(userId));
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
}
