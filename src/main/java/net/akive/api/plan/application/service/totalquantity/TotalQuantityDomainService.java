package net.akive.api.plan.application.service.totalquantity;

import lombok.RequiredArgsConstructor;
import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.required.SessionalPlanRepository;
import net.akive.api.plan.application.required.TotalQuantityRepository;
import net.akive.api.plan.domain.model.SessionalPlan;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.value.Subject;
import net.akive.api.plan.domain.value.TotalQuantityAmount;
import org.springframework.stereotype.Component;

import static net.akive.api.common.exception.business.ResourceNotFoundError.SESSIONAL_PLAN_NOT_FOUND;
import static net.akive.api.common.exception.business.ResourceNotFoundError.TOTAL_QUANTITY_NOT_FOUND;

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
            Subject targetSubject,
            TotalQuantityAmount targetTotalQuantityAmount
    ) {
        TotalQuantity totalQuantity = totalQuantityRepository
                .findBySessionalPlanIdAndUserIdAndTotalQuantityId(sessionalPlanId, userId, totalQuantityId)
                .orElseThrow(() -> new ResourceNotFoundException(TOTAL_QUANTITY_NOT_FOUND));

        totalQuantityValidationService.validateUpdatable(totalQuantityId, sessionalPlanId, targetSubject.name());

        totalQuantity.update(targetSubject, targetTotalQuantityAmount);

        return totalQuantity;
    }

    private SessionalPlan findSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return sessionalPlanRepository
                .findBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(SESSIONAL_PLAN_NOT_FOUND));
    }
}
