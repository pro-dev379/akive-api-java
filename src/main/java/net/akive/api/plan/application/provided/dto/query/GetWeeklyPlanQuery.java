package net.akive.api.plan.application.provided.dto.query;

public record GetWeeklyPlanQuery(
        Long sessionalPlanId,
        Long userId,
        Integer weekNum
) {
}
