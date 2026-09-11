package net.akive.api.user.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.user.adapter.webapi.dto.response.GoalFindResponse;
import net.akive.api.user.application.provided.GoalFinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members/me/goals")
@RequiredArgsConstructor
public class MemberGoalApi {

    private final GoalFinder goalFinder;

    @GetMapping("/latest")
    public GoalFindResponse findLatest(@AuthenticatedUserId Long userId) {
        return GoalFindResponse.from(goalFinder.findLatestByUserIdAndUserNotWithdrawn(userId));
    }
}
