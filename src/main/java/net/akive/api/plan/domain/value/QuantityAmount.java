package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNegative;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record QuantityAmount(int value) implements Comparable<QuantityAmount> {

    public static final QuantityAmount ZERO = new QuantityAmount(0);

    public QuantityAmount {
        requireNonNegative(value);
    }

    public QuantityAmount add(QuantityAmount amountToAdd) {
        requireNonNull(amountToAdd);

        return new QuantityAmount(this.value + amountToAdd.value);
    }

    public QuantityAmount subtract(QuantityAmount amountToSubtract) {
        requireNonNull(amountToSubtract);

        return new QuantityAmount(this.value - amountToSubtract.value);
    }

    public QuantityAmountDelta deltaTo(QuantityAmount after) {
        requireNonNull(after);

        return QuantityAmountDelta.calculate(this, after);
    }

    public static QuantityAmount min(QuantityAmount a, QuantityAmount b) {
        requireNonNull(a);
        requireNonNull(b);

        return a.value <= b.value ? a : b;
    }

    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isGreaterThan(QuantityAmount other) {
        requireNonNull(other);

        return this.value > other.value;
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
