package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.GetSessionalPlanUseCase;
import net.akive.api.plan.application.provided.GetSessionalPlansUseCase;
import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanQuery;
import net.akive.api.plan.application.provided.dto.result.common.SessionPeriodResult;
import net.akive.api.plan.application.provided.dto.result.common.SessionWeekResult;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlansResult;
import net.akive.api.plan.application.required.LoadSessionalPlanPort;
import net.akive.api.plan.application.required.LoadTotalQuantityPort;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.plan.domain.SessionalPlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class QuerySessionalPlanService implements GetSessionalPlansUseCase, GetSessionalPlanUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final LoadTotalQuantityPort loadTotalQuantityPort;

    @Override
    public GetSessionalPlansResult getAll(Long userId) {
        return GetSessionalPlansResult.from(
                loadSessionalPlanPort.loadAllByUserIdOrderBySessionalPlanIdDesc(userId)
        );
    }

    @Override
    public GetSessionalPlanResult get(GetSessionalPlanQuery query) {
        SessionalPlan sessionalPlan = loadSessionalPlanOrThrow(query.sessionalPlanId(), query.userId());
        List<SessionWeek> weeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();

        return new GetSessionalPlanResult(
                sessionalPlan.getSessionalPlanId(),
                sessionalPlan.getSessionName(),
                SessionPeriodResult.from(sessionalPlan.getSessionPeriod()),
                weeks.stream().map(SessionWeekResult::from).toList(),
                loadTotalQuantityPort.loadSessionalPlanTotalQuantitiesBySessionalPlanId(
                        sessionalPlan.getSessionalPlanId(),
                        weeks
                ).stream()
                        .map(GetSessionalPlanResult.TotalQuantityResult::from)
                        .toList()
        );
    }

    private SessionalPlan loadSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort
                .loadBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
