package net.akive.api.plan.application.service.sessionalplan;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.sessionalplan.SessionalPlanFinder;
import net.akive.api.plan.application.provided.sessionalplan.dto.SessionalPlanFindQuery;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.result.SessionalPlanDetail;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionWeek;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class SessionalPlanQueryService implements SessionalPlanFinder {

    private final SessionalPlanRepository sessionalPlanRepository;
    private final TotalQuantityRepository totalQuantityRepository;

    @Override
    public List<SessionalPlan> findAll(Long userId) {
        return sessionalPlanRepository.findAllByUserIdOrderBySessionalPlanIdDesc(userId);
    }

    @Override
    public SessionalPlanDetail find(SessionalPlanFindQuery query) throws NotFoundException {
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

    private SessionalPlan findSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return sessionalPlanRepository
                .findBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
