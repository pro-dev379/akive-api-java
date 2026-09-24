package net.akive.api.plan.domain.fixture;

import net.akive.api.plan.domain.value.QuantityCompletionAmount;
import net.akive.api.plan.domain.value.TotalQuantityDailyCompletionAmount;

import java.time.LocalDate;

public final class CompletionRateFixture {

    public static final LocalDate START_ON = LocalDate.of(2026, 9, 14);
    public static final LocalDate END_ON = LocalDate.of(2026, 9, 20);

    private CompletionRateFixture() {
    }

    public static TotalQuantityDailyCompletionAmount createDailyCompletionAmount(
            Long totalQuantityId,
            LocalDate date,
            int quantityAmount,
            int completedQuantityAmount
    ) {
        return new TotalQuantityDailyCompletionAmount(
                totalQuantityId,
                date,
                new QuantityCompletionAmount(quantityAmount, completedQuantityAmount)
        );
    }
}
