package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.DailyTodoResult;

import java.util.List;

public interface GetDailyTodosUseCase {

    List<DailyTodoResult> getTodosByDate(GetDailyTodosCommand command);
}
