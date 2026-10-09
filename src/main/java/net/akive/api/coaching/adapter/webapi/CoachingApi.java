package net.akive.api.coaching.adapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.coaching.adapter.webapi.request.CoachingConversationCreateRequest;
import net.akive.api.coaching.adapter.webapi.response.CoachingConversationCreateResponse;
import net.akive.api.coaching.application.provided.CoachingConversationCreator;
import net.akive.api.core.config.security.AuthenticatedUserId;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/coaching")
@RequiredArgsConstructor
public class CoachingApi {

    private final CoachingConversationCreator coachingConversationCreator;

    @PostMapping("/conversations")
    @ResponseStatus(CREATED)
    public CoachingConversationCreateResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody CoachingConversationCreateRequest request
    ) {
        Long coachingConversationId = coachingConversationCreator.create(request.to(userId));

        return new CoachingConversationCreateResponse(coachingConversationId);
    }
}
