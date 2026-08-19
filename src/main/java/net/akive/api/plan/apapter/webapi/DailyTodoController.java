package net.akive.api.plan.apapter.webapi;

import lombok.RequiredArgsConstructor;
import net.akive.api.core.config.security.AuthenticatedUserId;
import net.akive.api.plan.application.provided.GetDailyTodosUseCase;
import net.akive.api.plan.application.provided.dto.query.GetDailyTodosQuery;
import net.akive.api.plan.application.provided.dto.result.dailytodo.GetDailyTodosResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequestMapping("/sessional-plans/todos")
@RestController
@RequiredArgsConstructor
public class DailyTodoController {

    private final GetDailyTodosUseCase getDailyTodosUseCase;

    @GetMapping("/{date}")
    public GetDailyTodosResult getByDate(@AuthenticatedUserId Long userId, @PathVariable LocalDate date) {
        return getDailyTodosUseCase.getByDate(new GetDailyTodosQuery(userId, date));
    }
}
