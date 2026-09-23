package net.akive.api.plan.domain.value;

import java.math.BigDecimal;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;
import static java.math.RoundingMode.UNNECESSARY;
import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record CompletionRate(BigDecimal value) {

    private static final BigDecimal MAX_RATE = BigDecimal.valueOf(100);
    private static final int CALCULATION_SCALE = 12;

    public static CompletionRate calculate(List<QuantityCompletionAmount> completionAmounts) {
        requireNonNull(completionAmounts);

        if (completionAmounts.isEmpty()) {
            return new CompletionRate(BigDecimal.ZERO.setScale(1, UNNECESSARY));
        }

        BigDecimal summedCompletionRate = completionAmounts.stream()
                .map(CompletionRate::calculateRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageCompletionRate =
                summedCompletionRate.divide(BigDecimal.valueOf(completionAmounts.size()), 1, HALF_UP);

        return new CompletionRate(averageCompletionRate);
    }

    private static BigDecimal calculateRate(QuantityCompletionAmount amounts) {
        return BigDecimal.valueOf(amounts.completedQuantityAmount())
                .multiply(MAX_RATE)
                .divide(BigDecimal.valueOf(amounts.quantityAmount()), CALCULATION_SCALE, HALF_UP)
                .min(MAX_RATE);
    }
}
