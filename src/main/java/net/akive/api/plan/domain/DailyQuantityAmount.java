package net.akive.api.plan.domain;

import net.akive.api.plan.domain.exception.PlanException;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.plan.domain.exception.PlanErrorCode.DAILY_QUANTITY_AMOUNT_MISMATCH;

public record DailyQuantityAmount(
        QuantityAmount quantityAmount,
        QuantityAmount completedQuantityAmount,
        QuantityAmount delayedQuantityAmount,
        QuantityMemo quantityMemo
) {

    public DailyQuantityAmount {
        requireNonNull(quantityAmount);
        requireNonNull(completedQuantityAmount);
        requireNonNull(delayedQuantityAmount);
        quantityMemo = quantityMemo == null ? new QuantityMemo(null) : quantityMemo;

        int calculatedQuantityAmount = completedQuantityAmount.value() + delayedQuantityAmount.value();
        if (calculatedQuantityAmount != quantityAmount.value()) {
            throw new PlanException(DAILY_QUANTITY_AMOUNT_MISMATCH);
        }
    }

    public static DailyQuantityAmount of(
            int quantityAmount,
            int completedQuantityAmount,
            int delayedQuantityAmount,
            String quantityMemo
    ) {
        return new DailyQuantityAmount(
                new QuantityAmount(quantityAmount),
                new QuantityAmount(completedQuantityAmount),
                new QuantityAmount(delayedQuantityAmount),
                new QuantityMemo(quantityMemo)
        );
    }
}