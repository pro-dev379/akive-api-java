package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record TotalQuantityAmount(
        QuantityAmount quantityAmount,
        QuantityUnit quantityUnit,
        QuantityMemo quantityMemo
) {

    public TotalQuantityAmount {
        requireNonNull(quantityAmount);
        requireNonNull(quantityUnit);
        quantityMemo = quantityMemo == null ? new QuantityMemo(null) : quantityMemo;
    }

    public static TotalQuantityAmount of(int quantityAmount, String quantityUnit, String quantityMemo) {
        return new TotalQuantityAmount(
                new QuantityAmount(quantityAmount),
                new QuantityUnit(quantityUnit),
                new QuantityMemo(quantityMemo)
        );
    }
}
