package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNegative;

public record QuantityAmount(int value) {

    public QuantityAmount {
        requireNonNegative(value);
    }

    public QuantityAmount plus(int amount) {
        return new QuantityAmount(this.value + amount);
    }

    public QuantityAmount minus(int amount) {
        return new QuantityAmount(this.value - amount);
    }
}
