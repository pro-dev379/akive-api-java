package net.akive.api.plan.adapter.webapi.common.dto.response;

import net.akive.api.plan.domain.value.TotalQuantityAmount;

public record TotalQuantityAmountResponse(int quantityAmount, String quantityUnit, String quantityMemo) {

    public static TotalQuantityAmountResponse from(TotalQuantityAmount totalQuantityAmount) {
        return new TotalQuantityAmountResponse(
                totalQuantityAmount.quantityAmount().value(),
                totalQuantityAmount.quantityUnit().value(),
                totalQuantityAmount.quantityMemo().value()
        );
    }
}
