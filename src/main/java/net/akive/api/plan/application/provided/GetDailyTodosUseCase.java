package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.query.GetDailyTodosQuery;
import net.akive.api.plan.application.provided.dto.result.dailytodo.GetDailyTodosResult;

public interface GetDailyTodosUseCase {

    GetDailyTodosResult getByDate(GetDailyTodosQuery query);
}
