package net.akive.api.plan.application.dto.command;

public record DeleteTotalQuantityCommand(
        Long sessionalPlanId,
        Long userId,
        Long totalQuantityId
) {
}
