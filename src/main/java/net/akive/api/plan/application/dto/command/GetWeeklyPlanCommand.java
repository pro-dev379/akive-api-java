package net.akive.api.plan.application.dto.command;

public record GetWeeklyPlanCommand(
        Long sessionalPlanId,
        Long userId,
        Integer weekNum
) {
}