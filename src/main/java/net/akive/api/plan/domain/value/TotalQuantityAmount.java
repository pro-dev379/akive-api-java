package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record TotalQuantityAmount(
        QuantityAmount quantityAmount,
        QuantityUnit quantityUnit,
        QuantityMemo quantityMemo
) {

    public TotalQuantityAmount {
        validateRequiredValues(quantityAmount, quantityUnit);
        quantityMemo = quantityMemo == null ? QuantityMemo.NONE : quantityMemo;
    }

    public static TotalQuantityAmount of(int quantityAmount, String quantityUnit, String quantityMemo) {
        return new TotalQuantityAmount(
                new QuantityAmount(quantityAmount),
                new QuantityUnit(quantityUnit),
                new QuantityMemo(quantityMemo)
        );
    }

    public static TotalQuantityAmount of(int quantityAmount, String quantityUnit) {
        return new TotalQuantityAmount(
                new QuantityAmount(quantityAmount),
                new QuantityUnit(quantityUnit),
                QuantityMemo.NONE
        );
    }

    private static void validateRequiredValues(QuantityAmount quantityAmount, QuantityUnit quantityUnit) {
        requireNonNull(quantityAmount);
        requireNonNull(quantityUnit);
    }
}
