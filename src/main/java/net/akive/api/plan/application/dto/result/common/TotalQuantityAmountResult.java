package net.akive.api.plan.application.dto.result.common;

public record TotalQuantityAmountResult(
        int quantityAmount,
        String unit,
        String quantityMemo
) {
}
