package net.akive.api.plan.application.dto.command;

public record GetSessionalPlanDetailCommand(
        Long userId,
        Long sessionalPlanId
) {
}