package net.akive.api.plan.presentation;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.dto.result.DailyTodoResult;
import net.akive.api.plan.application.port.in.CreateSessionalPlanUseCase;
import net.akive.api.plan.application.port.in.GetSessionalPlansUseCase;
import net.akive.api.plan.application.port.in.GetDailyTodosUseCase;
import net.akive.api.plan.presentation.dto.request.CreateSessionalPlanRequest;
import net.akive.api.plan.presentation.dto.response.CreateSessionalPlanResponse;
import net.akive.api.plan.presentation.dto.response.GetSessionalPlansResponse;
import net.akive.api.plan.presentation.dto.response.GetDailyTodosResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/sessional-plans")
@RequiredArgsConstructor
public class SessionalPlanController {

    private final GetSessionalPlansUseCase getSessionalPlansUseCase;
    private final GetDailyTodosUseCase getDailyTodosUseCase;
    private final CreateSessionalPlanUseCase createSessionalPlanUseCase;

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
        List<DailyTodoResult> results = getDailyTodosUseCase.getTodosByDate(GetDailyTodosCommand.of(userId, date));

        return GetDailyTodosResponse.from(results);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateSessionalPlanResponse create(
            @AuthenticatedUserId Long userId,
            @Validated @RequestBody CreateSessionalPlanRequest request
    ) {
        SessionalPlanSummaryResult result = createSessionalPlanUseCase.create(request.toCommand(userId));

        return CreateSessionalPlanResponse.from(result);
    }
}