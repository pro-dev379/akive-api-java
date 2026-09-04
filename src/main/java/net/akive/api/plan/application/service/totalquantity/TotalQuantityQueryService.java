package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.TotalQuantityFinder;
import net.akive.api.plan.application.provided.totalquantity.dto.SessionalPlanTotalQuantityFindQuery;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.application.result.TotalQuantityProgress;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.SessionWeek;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class TotalQuantityQueryService implements TotalQuantityFinder {

    private final TotalQuantityRepository totalQuantityRepository;

    @Override
    public TotalQuantityProgress find(SessionalPlanTotalQuantityFindQuery query) throws ResourceNotFoundException {
        TotalQuantity totalQuantity = findTotalQuantityOrThrow(
                query.sessionalPlanId(),
                query.userId(),
                query.totalQuantityId()
        );
        List<SessionWeek> sessionWeeks = totalQuantity.getSessionalPlan().getSessionPeriod().splitIntoWeeks();

        return TotalQuantityProgress.of(totalQuantity, sessionWeeks);
    }

    private TotalQuantity findTotalQuantityOrThrow(
            Long sessionalPlanId,
            Long userId,
            Long totalQuantityId
    ) {
        return totalQuantityRepository
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId)
                .orElseThrow(() -> new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND));
    }
}
