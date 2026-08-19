package net.akive.api.plan.apapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.apapter.webapi.dto.request.CreateSessionalPlanRequest;
import net.akive.api.plan.apapter.webapi.dto.request.UpdateSessionalPlanRequest;
import net.akive.api.plan.apapter.webapi.dto.response.CreateSessionalPlanResponse;
import net.akive.api.plan.application.provided.*;
import net.akive.api.plan.application.provided.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanQuery;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlansResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/sessional-plans")
@RequiredArgsConstructor
public class SessionalPlanController {

    private final GetSessionalPlansUseCase getSessionalPlansUseCase;
    private final GetSessionalPlanUseCase getSessionalPlanUseCase;
    private final CreateSessionalPlanUseCase createSessionalPlanUseCase;
    private final UpdateSessionalPlanUseCase updateSessionalPlanUseCase;
    private final DeleteSessionalPlanUseCase deleteSessionalPlanUseCase;

    @GetMapping
    public GetSessionalPlansResult getAll(@AuthenticatedUserId Long userId) {
        return getSessionalPlansUseCase.getAll(userId);
    }

    @GetMapping("/{sessionalPlanId}")
    public GetSessionalPlanResult get(@AuthenticatedUserId Long userId, @PathVariable Long sessionalPlanId) {
        return getSessionalPlanUseCase.get(new GetSessionalPlanQuery(userId, sessionalPlanId));
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateSessionalPlanResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody CreateSessionalPlanRequest request
    ) {
       return new CreateSessionalPlanResponse(createSessionalPlanUseCase.create(request.to(userId)));
    }

    @PutMapping("/{sessionalPlanId}")
    @ResponseStatus(NO_CONTENT)
    public void update(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId,
            @Validated @RequestBody UpdateSessionalPlanRequest request
    ) {
        updateSessionalPlanUseCase.update(request.to(sessionalPlanId, userId));
    }

    @DeleteMapping("/{sessionalPlanId}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@AuthenticatedUserId Long userId, @PathVariable Long sessionalPlanId) {
        deleteSessionalPlanUseCase.delete(new DeleteSessionalPlanCommand(userId, sessionalPlanId));
    }
}
