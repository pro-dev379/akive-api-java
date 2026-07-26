package net.akive.api.plan.application.dto.query;

public record GetWeeklyPlanQuery(
        Long sessionalPlanId,
        Long userId,
        Integer weekNum
) {
}
