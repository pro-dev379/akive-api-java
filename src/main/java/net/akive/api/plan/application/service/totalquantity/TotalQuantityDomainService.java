package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import org.springframework.stereotype.Component;

import static net.akive.api.common.exception.business.NotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.NotFoundError.TOTAL_QUANTITY_NOT_FOUND;

@Component
@RequiredArgsConstructor
class TotalQuantityDomainService {

    private final SessionalPlanRepository sessionalPlanRepository;
    private final TotalQuantityRepository totalQuantityRepository;
    private final TotalQuantityValidationService totalQuantityValidationService;

    public TotalQuantity create(
            Long sessionalPlanId,
            Long userId,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        SessionalPlan sessionalPlan = findSessionalPlanOrThrow(sessionalPlanId, userId);

        totalQuantityValidationService.validateCreatable(sessionalPlanId, subject.name());

        return TotalQuantity.create(sessionalPlan, subject, totalQuantityAmount);
    }

    public TotalQuantity update(
            Long totalQuantityId,
            Long sessionalPlanId,
            Long userId,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        findSessionalPlanOrThrow(sessionalPlanId, userId);

        TotalQuantity totalQuantity = totalQuantityRepository
                .findBySessionalPlanIdAndTotalQuantityId(sessionalPlanId, totalQuantityId)
                .orElseThrow(() -> new NotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        totalQuantityValidationService.validateUpdatable(totalQuantityId, sessionalPlanId, subject.name());

        return totalQuantity.update(subject, totalQuantityAmount);
    }

    private SessionalPlan findSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return sessionalPlanRepository
                .findBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new NotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
