package net.akive.api.plan.domain;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record TotalQuantityPlan(
        TotalQuantity totalQuantity,
        List<WeeklyQuantityPlan> weeklyQuantities
) {

    public TotalQuantityPlan {
        requireNonNull(totalQuantity);
        weeklyQuantities = List.copyOf(requireNonNull(weeklyQuantities));
    }
}
