package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record QuantityAmountDelta(int value) implements Comparable<QuantityAmountDelta> {

    public static QuantityAmountDelta calculate(QuantityAmount before, QuantityAmount after) {
        requireNonNull(before);
        requireNonNull(after);

        return new QuantityAmountDelta(after.value() - before.value());
    }

    public QuantityAmount toAbsoluteAmount() {
        return new QuantityAmount(Math.abs(this.value));
    }

    public boolean isPositive() {
        return this.value > 0;
    }

    public boolean isNegative() {
        return this.value < 0;
    }

    public boolean isZero() {
        return this.value == 0;
    }

    @Override
    public int compareTo(QuantityAmountDelta other) {
        requireNonNull(other);

        return Integer.compare(this.value, other.value);
    }
}