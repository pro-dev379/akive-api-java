package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BadRequestException;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.exception.business.BadRequestError.*;

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

        QuantityAmount calculatedQuantityAmount = completedQuantityAmount.add(delayedQuantityAmount);

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

    public DailyQuantityAmount increaseBy(QuantityAmount amountToIncrease) {
        return new DailyQuantityAmount(
                this.quantityAmount.add(amountToIncrease),
                this.completedQuantityAmount,
                this.delayedQuantityAmount.add(amountToIncrease),
                this.quantityMemo
        );
    }

    public DailyQuantityAmount reduceBy(QuantityAmount amountToReduce) {
        QuantityAmount remainingAmount = this.quantityAmount.subtract(amountToReduce);

        if (remainingAmount.isLessThan(this.completedQuantityAmount)) {
            throw new BadRequestException(DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
        }

        return new DailyQuantityAmount(
                remainingAmount,
                this.completedQuantityAmount,
                this.delayedQuantityAmount.subtract(amountToReduce),
                this.quantityMemo
        );
    }

    public DailyQuantityAmount reduceBy(QuantityAmount amountToReduce, QuantityMemo targetMemo) {
        QuantityAmount remainingAmount = this.quantityAmount.subtract(amountToReduce);

        if (remainingAmount.isLessThan(this.completedQuantityAmount)) {
            throw new BadRequestException(DAILY_AMOUNT_LESS_THAN_COMPLETED_AMOUNT);
        }

        return new DailyQuantityAmount(
                remainingAmount,
                this.completedQuantityAmount,
                this.delayedQuantityAmount.subtract(amountToReduce),
                targetMemo
        );
    }

    public DailyQuantityAmount completeBy(QuantityAmount amountToComplete) {
        if (amountToComplete.isGreaterThan(this.delayedQuantityAmount)) {
            throw new BadRequestException(DAILY_AMOUNT_TO_COMPLETE_EXCEEDS_DELAYED_AMOUNT);
        }

        return new DailyQuantityAmount(
                this.quantityAmount,
                this.completedQuantityAmount.add(amountToComplete),
                this.delayedQuantityAmount.subtract(amountToComplete),
                this.quantityMemo
        );
    }

    public DailyQuantityAmount cancelCompletionBy(QuantityAmount amountToCancelCompletion) {
        if (amountToCancelCompletion.isGreaterThan(this.completedQuantityAmount)) {
            throw new BadRequestException(DAILY_AMOUNT_TO_CANCEL_COMPLETION_EXCEEDS_COMPLETED_AMOUNT);
        }

        return new DailyQuantityAmount(
                this.quantityAmount,
                this.completedQuantityAmount.subtract(amountToCancelCompletion),
                this.delayedQuantityAmount.add(amountToCancelCompletion),
                this.quantityMemo
        );
    }

    public DailyQuantityAmount updateMemo(QuantityMemo targetMemo) {
        return new DailyQuantityAmount(
                this.quantityAmount,
                this.completedQuantityAmount,
                this.delayedQuantityAmount,
                targetMemo
        );
    }
}
