package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record WeeklyQuantityAmount(QuantityAmount quantityAmount, QuantityMemo quantityMemo) {

    public WeeklyQuantityAmount {
        requireNonNull(quantityAmount);
        quantityMemo = quantityMemo == null ? QuantityMemo.NONE : quantityMemo;
    }

    public static WeeklyQuantityAmount of(int quantityAmount, String quantityMemo) {
        return new WeeklyQuantityAmount(new QuantityAmount(quantityAmount), new QuantityMemo(quantityMemo));
    }

    public static WeeklyQuantityAmount of(int quantityAmount) {
        return new WeeklyQuantityAmount(new QuantityAmount(quantityAmount), QuantityMemo.NONE);
    }
}
