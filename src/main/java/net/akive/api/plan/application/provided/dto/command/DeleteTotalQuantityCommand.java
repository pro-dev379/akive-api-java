package net.akive.api.plan.application.provided.dto.command;

public record DeleteTotalQuantityCommand(
        Long sessionalPlanId,
        Long userId,
        Long totalQuantityId
) {
}
