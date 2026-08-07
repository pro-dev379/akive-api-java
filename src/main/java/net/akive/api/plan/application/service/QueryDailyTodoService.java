package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.provided.dto.query.GetDailyTodosQuery;
import net.akive.api.plan.application.provided.dto.result.dailytodo.GetDailyTodosResult;
import net.akive.api.plan.application.provided.GetDailyTodosUseCase;
import net.akive.api.plan.application.required.LoadDailyTodosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class QueryDailyTodoService implements GetDailyTodosUseCase {

    private final LoadDailyTodosPort loadDailyTodosPort;

    @Override
    public GetDailyTodosResult getByDate(GetDailyTodosQuery query) {
        return GetDailyTodosResult.from(
                loadDailyTodosPort.loadDailyTodosByUserIdAndDate(query.userId(), query.date())
        );
    }
}
