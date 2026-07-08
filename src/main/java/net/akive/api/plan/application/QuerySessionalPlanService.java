package net.akive.api.plan.application;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;
import net.akive.api.plan.application.dto.result.DailyTodoResult;
import net.akive.api.plan.application.port.in.GetSessionalPlansUseCase;
import net.akive.api.plan.application.port.in.GetDailyTodosUseCase;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadDailyTodosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class QuerySessionalPlanService implements GetSessionalPlansUseCase, GetDailyTodosUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final LoadDailyTodosPort loadDailyTodosPort;

    @Override
    public List<SessionalPlanSummaryResult> getAll(Long userId) {
        return loadSessionalPlanPort
                .loadAllByUserIdOrderBySessionalPlanIdDesc(userId)
                .stream()
                .map(SessionalPlanSummaryResult::from)
                .toList();
    }

    @Override
    public List<DailyTodoResult> getTodosByDate(GetDailyTodosCommand command) {
        return loadDailyTodosPort.loadDailyTodosByUserIdAndDate(command.userId(), command.date());
    }
}