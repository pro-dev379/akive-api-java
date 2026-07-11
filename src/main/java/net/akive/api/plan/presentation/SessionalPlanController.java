package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;
import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.dto.result.DailyTodoResult;
import net.akive.api.plan.application.port.in.*;
import net.akive.api.plan.presentation.dto.request.CreateSessionalPlanRequest;
import net.akive.api.plan.presentation.dto.request.UpdateSessionalPlanRequest;
import net.akive.api.plan.presentation.dto.response.CreateSessionalPlanResponse;
import net.akive.api.plan.presentation.dto.response.GetSessionalPlansResponse;
import net.akive.api.plan.presentation.dto.response.GetDailyTodosResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/sessional-plans")
@RequiredArgsConstructor
public class SessionalPlanController {

    private final GetSessionalPlansUseCase getSessionalPlansUseCase;
    private final GetDailyTodosUseCase getDailyTodosUseCase;
    private final CreateSessionalPlanUseCase createSessionalPlanUseCase;
    private final UpdateSessionalPlanUseCase updateSessionalPlanUseCase;
    private final DeleteSessionalPlanUseCase deleteSessionalPlanUseCase;

    @GetMapping
    public GetSessionalPlansResponse getAll(@AuthenticatedUserId Long userId) {
        List<SessionalPlanSummaryResult> results = getSessionalPlansUseCase.getAll(userId);

        return GetSessionalPlansResponse.from(results);
    }

    @GetMapping("/todos/{date}")
    public GetDailyTodosResponse getTodosByDate(
            @AuthenticatedUserId Long userId,
            @PathVariable LocalDate date
    ) {
        List<DailyTodoResult> results = getDailyTodosUseCase.getTodosByDate(new GetDailyTodosCommand(userId, date));

        return GetDailyTodosResponse.from(results);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateSessionalPlanResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody CreateSessionalPlanRequest request
    ) {
        SessionalPlanSummaryResult result = createSessionalPlanUseCase.create(request.to(userId));

        return CreateSessionalPlanResponse.from(result);
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