package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.command.GetDailyTodosCommand;
import net.akive.api.plan.application.dto.command.GetSessionalPlanDetailCommand;
import net.akive.api.plan.application.dto.result.common.SessionPeriodResult;
import net.akive.api.plan.application.dto.result.dailytodo.GetDailyTodosResult;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanDetailResult;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlansResult;
import net.akive.api.plan.application.port.in.GetSessionalPlanDetailUseCase;
import net.akive.api.plan.application.port.in.GetSessionalPlansUseCase;
import net.akive.api.plan.application.port.in.GetDailyTodosUseCase;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadDailyTodosPort;
import net.akive.api.plan.application.port.out.LoadTotalQuantitiesPort;
import net.akive.api.plan.domain.SessionalPlan;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.plan.exception.PlanException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class QuerySessionalPlanService implements GetSessionalPlansUseCase, GetSessionalPlanDetailUseCase, GetDailyTodosUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final LoadDailyTodosPort loadDailyTodosPort;
    private final LoadTotalQuantitiesPort loadTotalQuantitiesPort;

    @Override
    public GetSessionalPlansResult getAll(Long userId) {
        return GetSessionalPlansResult.from(
                loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(userId)
        );
    }

    @Override
    public GetSessionalPlanDetailResult getDetail(GetSessionalPlanDetailCommand command) {
        SessionalPlan sessionalPlan = loadSessionalPlan(command.sessionalPlanId(), command.userId());
        List<SessionWeek> weeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();

        return new GetSessionalPlanDetailResult(
                sessionalPlan.getSessionalPlanId(),
                sessionalPlan.getSessionName(),
                SessionPeriodResult.from(sessionalPlan.getSessionPeriod()),
                weeks.stream().map(GetSessionalPlanDetailResult.SessionWeekResult::from).toList(),
                loadTotalQuantitiesPort.loadSessionalPlanTotalQuantitiesBySessionalPlanId(
                        sessionalPlan.getSessionalPlanId(),
                        weeks
                ).stream()
                        .map(GetSessionalPlanDetailResult.TotalQuantityResult::from)
                        .toList()
        );
    }

    @Override
    public GetDailyTodosResult getTodosByDate(GetDailyTodosCommand command) {
        return GetDailyTodosResult.from(
                loadDailyTodosPort.loadDailyTodosByUserIdAndDate(command.userId(), command.date())
        );
    }

    private SessionalPlan loadSessionalPlan(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort
                .loadBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new PlanException(SESSIONAL_PLAN_NOT_FOUND));
    }
}