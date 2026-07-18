package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.dailytodo.GetDailyTodosResult;

public interface GetDailyTodosUseCase {

    GetDailyTodosResult getByDate(GetDailyTodosCommand command);
}
