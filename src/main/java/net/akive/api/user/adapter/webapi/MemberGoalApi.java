package net.akive.api.user.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.user.adapter.webapi.dto.request.GoalCreateRequest;
import net.akive.api.user.adapter.webapi.dto.response.GoalFindResponse;
import net.akive.api.user.application.provided.GoalCreator;
import net.akive.api.user.application.provided.GoalFinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/members/me/goals/latest")
@RequiredArgsConstructor
public class MemberGoalApi {

    private final GoalCreator goalCreator;
    private final GoalFinder goalFinder;

    @PostMapping
    @ResponseStatus(CREATED)
    public void create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody GoalCreateRequest request
    ) {
        goalCreator.create(request.to(userId));
    }

    @GetMapping
    public GoalFindResponse findLatest(@AuthenticatedUserId Long userId) {
        return GoalFindResponse.from(goalFinder.findLatestByUserIdAndUserNotWithdrawn(userId));
    }
}
