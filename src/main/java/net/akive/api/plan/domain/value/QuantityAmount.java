package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNegative;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record QuantityAmount(int value) implements Comparable<QuantityAmount> {

    public static final QuantityAmount ZERO = new QuantityAmount(0);

    public QuantityAmount {
        requireNonNegative(value);
    }

    public QuantityAmount plus(QuantityAmount amount) {
        requireNonNull(amount);

        return new QuantityAmount(this.value + amount.value);
    }

    public QuantityAmount minus(QuantityAmount amount) {
        requireNonNull(amount);

        return new QuantityAmount(this.value - amount.value);
    }

    public QuantityAmountDelta deltaTo(QuantityAmount after) {
        requireNonNull(after);

        return QuantityAmountDelta.calculate(this.value, after.value);
    }

    public static QuantityAmount min(QuantityAmount a, QuantityAmount b) {
        requireNonNull(a);
        requireNonNull(b);

        return a.value <= b.value ? a : b;
    }

    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isLessThan(QuantityAmount other) {
        requireNonNull(other);

        return this.value < other.value;
    }

    @Override
    public int compareTo(QuantityAmount other) {
        requireNonNull(other);

        return Integer.compare(this.value, other.value);
    }
}
