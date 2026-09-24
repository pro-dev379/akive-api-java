package net.akive.api.plan.domain.value;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record TotalQuantityDailyCompletionAmount(
        Long totalQuantityId,
        LocalDate date,
        QuantityCompletionAmount completionAmount
) {

    public TotalQuantityDailyCompletionAmount {
        requirePositive(totalQuantityId);
        requireNonNull(date);
        requireNonNull(completionAmount);
    }
}
