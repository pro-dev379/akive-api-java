package net.akive.api.plan.application.dto.result.common;

import net.akive.api.plan.domain.DailyQuantityAmount;

public record DailyQuantityAmountResult(
        int quantityAmount,
        int completedQuantityAmount,
        int delayedQuantityAmount,
        String quantityMemo
) {

    public static DailyQuantityAmountResult from(DailyQuantityAmount dailyQuantityAmount) {
        return new DailyQuantityAmountResult(
                dailyQuantityAmount.quantityAmount().value(),
                dailyQuantityAmount.completedQuantityAmount().value(),
                dailyQuantityAmount.delayedQuantityAmount().value(),
                dailyQuantityAmount.quantityMemo().value()
        );
    }
}
