package net.akive.api.plan.application.required.dto;

public record SessionalPlanWeeklyQuantityAmountData(
        int quantityAmount,
        int completedQuantityAmount,
        int delayedQuantityAmount,
        String quantityMemo
) {
}
