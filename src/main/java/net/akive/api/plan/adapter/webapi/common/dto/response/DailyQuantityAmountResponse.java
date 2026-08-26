package net.akive.api.plan.adapter.webapi.common.dto.response;

import net.akive.api.plan.domain.value.DailyQuantityAmount;

public record DailyQuantityAmountResponse(
        int quantityAmount,
        int completedQuantityAmount,
        int delayedQuantityAmount,
        String quantityMemo
) {

    public static DailyQuantityAmountResponse from(DailyQuantityAmount dailyQuantityAmount) {
        return new DailyQuantityAmountResponse(
                dailyQuantityAmount.quantityAmount().value(),
                dailyQuantityAmount.completedQuantityAmount().value(),
                dailyQuantityAmount.delayedQuantityAmount().value(),
                dailyQuantityAmount.quantityMemo().value()
        );
    }
}
