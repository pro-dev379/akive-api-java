package net.akive.api.plan.domain.value;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record QuantityAmountDelta(int value) implements Comparable<QuantityAmountDelta> {

        public boolean isPositive() {
            return this.value > 0;
        }

        public boolean isNegative() {
            return this.value < 0;
        }

        public boolean isZero() {
            return this.value == 0;
        }

        public QuantityAmount absoluteAmount() {
            return new QuantityAmount(Math.abs(this.value));
        }

        public static QuantityAmountDelta calculate(int before, int after) {
            return new QuantityAmountDelta(after - before);
        }

    @Override
    public int compareTo(QuantityAmountDelta other) {
        requireNonNull(other);

        return Integer.compare(this.value, other.value);
    }
}