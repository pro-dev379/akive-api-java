package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.dailytodo.GetDailyTodosResult;
import net.akive.api.plan.application.port.in.GetDailyTodosUseCase;
import net.akive.api.plan.application.port.out.LoadDailyTodosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class QueryDailyTodoService implements GetDailyTodosUseCase {

    private final LoadDailyTodosPort loadDailyTodosPort;

    @Override
    public GetDailyTodosResult getByDate(GetDailyTodosCommand command) {
        return GetDailyTodosResult.from(
                loadDailyTodosPort.loadDailyTodosByUserIdAndDate(command.userId(), command.date())
        );
    }
}