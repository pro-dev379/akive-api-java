package net.akive.api.plan.application.dto.command;

public record GetSessionalPlanCommand(
        Long userId,
        Long sessionalPlanId
) {
}