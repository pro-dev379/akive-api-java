package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNegative;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record QuantityCompletionAmount(int quantityAmount, int completedQuantityAmount) {

    public QuantityCompletionAmount {
        requirePositive(quantityAmount);
        requireNonNegative(completedQuantityAmount);
    }
}
