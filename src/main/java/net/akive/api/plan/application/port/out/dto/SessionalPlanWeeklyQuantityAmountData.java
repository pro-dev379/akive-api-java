package net.akive.api.plan.application.port.out.dto;

public record SessionalPlanWeeklyQuantityAmountData(
        int quantityAmount,
        int completedQuantityAmount,
        int delayedQuantityAmount,
        String quantityMemo
) {
}
