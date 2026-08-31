package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BadRequestException;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.exception.business.BadRequestError.DAILY_AMOUNT_MISMATCH;

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
        quantityMemo = quantityMemo == null ? QuantityMemo.NONE : quantityMemo;

        QuantityAmount calculatedQuantityAmount = completedQuantityAmount.plus(delayedQuantityAmount);

        if (!calculatedQuantityAmount.equals(quantityAmount)) {
            throw new BadRequestException(DAILY_AMOUNT_MISMATCH);
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

    public static DailyQuantityAmount of(
            int quantityAmount,
            int completedQuantityAmount,
            int delayedQuantityAmount
    ) {
        return new DailyQuantityAmount(
                new QuantityAmount(quantityAmount),
                new QuantityAmount(completedQuantityAmount),
                new QuantityAmount(delayedQuantityAmount),
                QuantityMemo.NONE
        );
    }
}
