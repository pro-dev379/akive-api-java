package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.dto.query.GetWeeklyPlanQuery;
import net.akive.api.plan.application.dto.result.common.SessionWeekResult;
import net.akive.api.plan.application.dto.result.weeklyplan.GetWeeklyPlanResult;
import net.akive.api.plan.application.port.in.GetWeeklyPlanUseCase;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.application.port.out.LoadWeeklyQuantityPort;
import net.akive.api.plan.application.port.out.dto.WeeklyPlanWeeklyQuantityData;
import net.akive.api.plan.domain.PlanDate;
import net.akive.api.plan.domain.SessionPeriod;
import net.akive.api.plan.domain.SessionWeek;
import net.akive.api.common.exception.business.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.WEEKLY_PLAN_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class QueryWeeklyPlanService implements GetWeeklyPlanUseCase {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final LoadWeeklyQuantityPort loadWeeklyQuantityPort;

    @Override
    public GetWeeklyPlanResult getByWeekNum(GetWeeklyPlanQuery query) {
        SessionPeriod sessionPeriod = loadSessionPeriodOrThrow(query.sessionalPlanId(), query.userId());
        SessionWeek sessionWeek = getSessionWeekOrThrow(sessionPeriod, query.weekNum());
        List<PlanDate> planDates = sessionWeek.weekPeriod().splitIntoDates();

        List<WeeklyPlanWeeklyQuantityData> weeklyQuantities =
                loadWeeklyQuantityPort.loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
                        query.sessionalPlanId(),
                        sessionWeek.weekPeriod()
                );

        return new GetWeeklyPlanResult(
                SessionWeekResult.from(sessionWeek),
                planDates.stream().map(GetWeeklyPlanResult.PlanDateResult::from).toList(),
                weeklyQuantities.stream()
                        .map(GetWeeklyPlanResult.WeeklyQuantityResult::from)
                        .toList()
        );
    }

    private SessionPeriod loadSessionPeriodOrThrow(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort.loadSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }

    private SessionWeek getSessionWeekOrThrow(SessionPeriod sessionPeriod, Integer weekNum) {
        if (weekNum == null) {
            throw new NotFoundException(WEEKLY_PLAN_NOT_FOUND);
        }

        return sessionPeriod.findSessionWeek(weekNum)
                .orElseThrow(() -> new NotFoundException(WEEKLY_PLAN_NOT_FOUND));
    }
}
