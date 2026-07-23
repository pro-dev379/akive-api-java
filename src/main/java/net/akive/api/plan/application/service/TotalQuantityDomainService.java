package net.akive.api.plan.application.service;

import lombok.RequiredArgsConstructor;
import net.akive.api.plan.application.port.out.LoadSessionalPlanPort;
import net.akive.api.plan.domain.*;
import net.akive.api.plan.exception.PlanException;
import org.springframework.stereotype.Component;

import static net.akive.api.plan.exception.PlanErrorCode.SESSIONAL_PLAN_NOT_FOUND;

@Component
@RequiredArgsConstructor
class TotalQuantityDomainService {

    private final LoadSessionalPlanPort loadSessionalPlanPort;
    private final TotalQuantityValidator totalQuantityValidator;

    private final TotalQuantityPlanFactory totalQuantityPlanFactory = new TotalQuantityPlanFactory();

    public TotalQuantityPlan create(
            Long sessionalPlanId,
            Long userId,
            Subject subject,
            TotalQuantityAmount totalQuantityAmount
    ) {
        SessionalPlan sessionalPlan = loadSessionalPlanOrThrow(sessionalPlanId, userId);
        totalQuantityValidator.validateCreatable(sessionalPlanId, subject.name());

        return totalQuantityPlanFactory.create(
                sessionalPlan,
                subject,
                totalQuantityAmount
        );
    }

    private SessionalPlan loadSessionalPlanOrThrow(Long sessionalPlanId, Long userId) {
        return loadSessionalPlanPort
                .loadBySessionalPlanIdAndUserId(sessionalPlanId, userId)
                .orElseThrow(() -> new PlanException(SESSIONAL_PLAN_NOT_FOUND));
    }
}