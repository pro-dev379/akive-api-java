package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.command.GetSessionalPlanDetailCommand;
import net.akive.api.plan.application.dto.result.dailytodo.GetDailyTodosResult;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanDetailResult;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlansResult;
import net.akive.api.plan.application.port.in.*;
import net.akive.api.plan.presentation.dto.request.CreateSessionalPlanRequest;
import net.akive.api.plan.presentation.dto.request.UpdateSessionalPlanRequest;
import net.akive.api.plan.presentation.dto.response.CreateSessionalPlanResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/sessional-plans")
@RequiredArgsConstructor
public class SessionalPlanController {

    private final GetSessionalPlansUseCase getSessionalPlansUseCase;
    private final GetSessionalPlanDetailUseCase getSessionalPlanDetailUseCase;
    private final GetDailyTodosUseCase getDailyTodosUseCase;
    private final CreateSessionalPlanUseCase createSessionalPlanUseCase;
    private final UpdateSessionalPlanUseCase updateSessionalPlanUseCase;
    private final DeleteSessionalPlanUseCase deleteSessionalPlanUseCase;

    @GetMapping
    public GetSessionalPlansResult getAll(@AuthenticatedUserId Long userId) {
        return getSessionalPlansUseCase.getAll(userId);
    }

    @GetMapping("/{sessionalPlanId}")
    public GetSessionalPlanDetailResult getDetail(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId
    ) {
        return getSessionalPlanDetailUseCase.getDetail(new GetSessionalPlanDetailCommand(userId, sessionalPlanId));
    }

    @GetMapping("/todos/{date}")
    public GetDailyTodosResult getTodosByDate(
            @AuthenticatedUserId Long userId,
            @PathVariable LocalDate date
    ) {
        return getDailyTodosUseCase.getTodosByDate(new GetDailyTodosCommand(userId, date));
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
    public void delete(
            @AuthenticatedUserId Long userId,
            @PathVariable Long sessionalPlanId
    ) {
        deleteSessionalPlanUseCase.delete(new DeleteSessionalPlanCommand(userId, sessionalPlanId));
    }
}