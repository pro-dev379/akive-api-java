package net.akive.api.plan.application.provided.totalquantity.dto;

import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record WeeklyPlanItemFindQuery(
        Long sessionalPlanId,
        Long userId,
        Long totalQuantityId,
        Long weeklyQuantityId
) {

    public WeeklyPlanItemFindQuery {
        requirePositive(sessionalPlanId);
        requirePositive(totalQuantityId);
        requirePositive(weeklyQuantityId);
    }
}
