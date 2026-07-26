package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.query.GetDailyTodosQuery;
import net.akive.api.plan.application.dto.result.dailytodo.GetDailyTodosResult;

public interface GetDailyTodosUseCase {

    GetDailyTodosResult getByDate(GetDailyTodosQuery query);
}
