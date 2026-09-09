package net.akive.api.user.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.user.adapter.webapi.dto.response.UserFindResponse;
import net.akive.api.user.application.provided.UserFinder;
import net.akive.api.user.domain.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members/me")
@RequiredArgsConstructor
public class MemberAccountPrivateApi {

    private final UserFinder userFinder;

    @GetMapping
    public UserFindResponse find(@AuthenticatedUserId Long userId) {
        User foundUser = userFinder.findOrThrowByUserIdAndNotWithdrawn(userId);

        return UserFindResponse.from(foundUser);
    }
}
