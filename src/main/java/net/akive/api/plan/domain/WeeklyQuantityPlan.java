package net.akive.api.plan.domain;

import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record WeeklyQuantityPlan(
        WeeklyQuantity weeklyQuantity,
        List<DailyQuantity> dailyQuantities
) {

    public WeeklyQuantityPlan {
        requireNonNull(weeklyQuantity);
        dailyQuantities = List.copyOf(requireNonNull(dailyQuantities));
    }
}