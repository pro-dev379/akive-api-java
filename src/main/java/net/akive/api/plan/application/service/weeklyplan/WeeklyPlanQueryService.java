package net.akive.api.plan.application.service.weeklyplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.weeklyplan.WeeklyPlanFinder;
import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.WeeklyQuantityRepository;
import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.application.result.WeeklyPlan;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.plan.domain.value.SessionWeek;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.WEEKLY_PLAN_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class WeeklyPlanQueryService implements WeeklyPlanFinder {

    private final SessionalPlanRepository sessionalPlanRepository;
    private final WeeklyQuantityRepository weeklyQuantityRepository;

    @Override
    public WeeklyPlan find(WeeklyPlanFindQuery query) throws ResourceNotFoundException {
        SessionPeriod sessionPeriod = findSessionPeriodOrThrow(query.sessionalPlanId(), query.userId());
        SessionWeek sessionWeek = getSessionWeekOrThrow(sessionPeriod, query.weekNum());

        List<WeeklyPlan.WeeklyPlanItem> weeklyPlanItems =
                weeklyQuantityRepository
                        .findAllWeeklyPlanDataBySessionalPlanIdAndUserIdAndWeekPeriodOrderByPlanHierarchyAsc(
                                query.sessionalPlanId(),
                                query.userId(),
                                sessionWeek.weekPeriod()
                        )
                        .stream()
                        .map(this::toWeeklyPlanItem)
                        .toList();

        return new WeeklyPlan(sessionWeek, weeklyPlanItems);
    }

    private WeeklyPlan.WeeklyPlanItem toWeeklyPlanItem(WeeklyPlanData weeklyPlanData) {
        return new WeeklyPlan.WeeklyPlanItem(
                weeklyPlanData.sessionalPlanId(),
                weeklyPlanData.totalQuantityId(),
                weeklyPlanData.subject(),
                weeklyPlanData.quantityUnit(),
                weeklyPlanData.weeklyQuantity()
        );
    }

    private SessionPeriod findSessionPeriodOrThrow(Long sessionalPlanId, Long userId) {
        return sessionalPlanRepository.findSessionPeriodBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }

    private SessionWeek getSessionWeekOrThrow(SessionPeriod sessionPeriod, Integer weekNum) {
        if (weekNum == null) {
            throw new ResourceNotFoundException(WEEKLY_PLAN_NOT_FOUND);
        }

        return sessionPeriod.findSessionWeek(weekNum)
                .orElseThrow(() -> new ResourceNotFoundException(WEEKLY_PLAN_NOT_FOUND));
    }
}
