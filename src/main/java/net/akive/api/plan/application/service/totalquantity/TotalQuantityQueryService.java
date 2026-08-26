package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.dto.SessionalPlanTotalQuantityFindQuery;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionWeek;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.TOTAL_QUANTITY_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class TotalQuantityQueryService implements TotalQuantityFinder {

    private final SessionalPlanRepository sessionalPlanRepository;
    private final TotalQuantityRepository totalQuantityRepository;

    @Override
    public TotalQuantityProgress find(SessionalPlanTotalQuantityFindQuery query) throws NotFoundException {
        SessionalPlan sessionalPlan = findSessionalPlanOrThrow(query.sessionalPlanId(), query.userId());
        List<SessionWeek> sessionWeeks = sessionalPlan.getSessionPeriod().splitIntoWeeks();

        TotalQuantity totalQuantity = findSessionalPlanTotalQuantityOrThrow(
                sessionalPlan.getSessionalPlanId(),
                query.totalQuantityId()
        );

        return TotalQuantityProgress.of(totalQuantity, sessionWeeks);
    }

    private SessionalPlan findSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return sessionalPlanRepository.findBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }

    private TotalQuantity findSessionalPlanTotalQuantityOrThrow(
            Long sessionalPlanId,
            Long totalQuantityId
    ) {
        return totalQuantityRepository
                .findBySessionalPlanIdAndTotalQuantityId(
                        sessionalPlanId,
                        totalQuantityId
                )
                .orElseThrow(() -> new NotFoundException(TOTAL_QUANTITY_NOT_FOUND));
    }
}
