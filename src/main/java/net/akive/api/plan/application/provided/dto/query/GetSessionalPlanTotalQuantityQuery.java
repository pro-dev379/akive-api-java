package net.akive.api.plan.application.provided.dto.query;

public record GetSessionalPlanTotalQuantityQuery(
        Long sessionalPlanId,
        Long userId,
        Long totalQuantityId
) {
}
