package net.akive.api.plan.application.dto.query;

public record GetSessionalPlanTotalQuantityQuery(
        Long sessionalPlanId,
        Long userId,
        Long totalQuantityId
) {
}
