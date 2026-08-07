package net.akive.api.plan.application.provided.dto.query;

public record GetSessionalPlanQuery(
        Long userId,
        Long sessionalPlanId
) {
}
