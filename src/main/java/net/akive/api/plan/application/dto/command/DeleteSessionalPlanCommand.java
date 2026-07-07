package net.akive.api.plan.application.dto.command;

public record DeleteSessionalPlanCommand(Long userId, Long sessionalPlanId) {

    public static DeleteSessionalPlanCommand of(Long userId, Long sessionalPlanId) {
        return new DeleteSessionalPlanCommand(userId, sessionalPlanId);
    }
}
