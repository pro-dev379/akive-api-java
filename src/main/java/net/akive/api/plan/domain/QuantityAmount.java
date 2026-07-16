package net.akive.api.plan.domain;

import static net.akive.api.common.domain.DomainValidator.requireNonNegative;

public record QuantityAmount(int value) {

    public QuantityAmount {
        requireNonNegative(value);
    }
}