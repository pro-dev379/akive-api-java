package net.akive.api.plan.application.service.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.CompletionRateFindQuery;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanDetailFindQuery;
import net.akive.api.plan.application.required.DailyQuantityRepository;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.CompletionRate;
import net.akive.api.plan.domain.value.QuantityCompletionAmount;
import net.akive.api.plan.domain.value.SessionPeriod;
import net.akive.api.plan.domain.value.SessionWeek;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class SessionalPlanQueryService implements SessionalPlanFinder {

    private final SessionalPlanRepository sessionalPlanRepository;
    private final TotalQuantityRepository totalQuantityRepository;
    private final DailyQuantityRepository dailyQuantityRepository;

    @Override
    public List<SessionalPlan> findAll(Long userId) {
        return sessionalPlanRepository.findAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Override
    public SessionalPlanDetail findDetail(SessionalPlanDetailFindQuery query) throws ResourceNotFoundException {
        SessionalPlan sessionalPlan = findSessionalPlanOrThrow(query.sessionalPlanId(), query.userId());
        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();

        List<TotalQuantity> totalQuantities =
                totalQuantityRepository.findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(
                        sessionalPlan.getSessionalPlanId()
                );
        List<TotalQuantityProgress> totalQuantityProgresses = totalQuantities.stream()
                .map(totalQuantity -> TotalQuantityProgress.of(totalQuantity, sessionWeeks))
                .toList();

        return new SessionalPlanDetail(sessionalPlan, totalQuantityProgresses);
    }

    @Override
    public List<SessionPeriod> findSessionPeriodsByUserId(Long userId) {
        return sessionalPlanRepository.findSessionPeriodsByUserId(userId);
    }

    @Override
    public CompletionRate findCompletionRate(CompletionRateFindQuery query) {
        List<QuantityCompletionAmount> completionAmounts =
                dailyQuantityRepository.findTotalQuantityCompletionAmountsByUserIdAndDateBetween(
                        query.userId(),
                        query.startOn(),
                        query.endOn()
                );

        return CompletionRate.calculate(completionAmounts);
    }

    private SessionalPlan findSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return sessionalPlanRepository
                .findBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
