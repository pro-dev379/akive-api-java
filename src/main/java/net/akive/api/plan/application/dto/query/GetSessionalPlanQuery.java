package net.akive.api.plan.application.dto.query;

public record GetSessionalPlanQuery(
        Long userId,
        Long sessionalPlanId
) {
}