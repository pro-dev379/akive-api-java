package net.akive.api.plan.domain;

import static net.akive.api.common.domain.DomainValidator.*;
import static net.akive.api.plan.domain.PlanPolicy.QUANTITY_UNIT_MAX_LENGTH;

public record TotalQuantityAmount(QuantityAmount quantityAmount, String unit, QuantityMemo quantityMemo) {

    public TotalQuantityAmount {
        requireNonNull(quantityAmount);
        unit = requireMaxLength(requireNotBlank(unit), QUANTITY_UNIT_MAX_LENGTH);
        quantityMemo = quantityMemo == null ? new QuantityMemo(null) : quantityMemo;
    }

    public static TotalQuantityAmount of(int quantityAmount, String unit, String quantityMemo) {
        return new TotalQuantityAmount(
                new QuantityAmount(quantityAmount),
                unit,
                new QuantityMemo(quantityMemo)
        );
    }
}
