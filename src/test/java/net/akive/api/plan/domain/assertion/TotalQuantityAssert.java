package net.akive.api.plan.domain.assertion;

import net.akive.api.plan.domain.model.TotalQuantity;
import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;

import static net.akive.api.plan.domain.assertion.WeeklyQuantityAssert.assertThatWeeklyQuantity;
import static org.assertj.core.api.Assertions.assertThat;

public class TotalQuantityAssert extends AbstractAssert<TotalQuantityAssert, TotalQuantity> {

    private TotalQuantityAssert(TotalQuantity actual) {
        super(actual, TotalQuantityAssert.class);
    }

    public static TotalQuantityAssert assertThatTotalQuantity(TotalQuantity actual) {
        return new TotalQuantityAssert(actual);
    }

    public TotalQuantityAssert hasWeeklyAmounts(int... expectedWeeklyAmounts) {
        isNotNull();

        assertThat(actual.getWeeklyQuantities())
                .extracting(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount()
                        .quantityAmount()
                        .value())
                .containsExactlyElementsOf(Arrays.stream(expectedWeeklyAmounts).boxed().toList());
        hasWeeklyAmountSumEqualToTotalAmount();
        actual.getWeeklyQuantities()
                .forEach(weeklyQuantity -> assertThatWeeklyQuantity(weeklyQuantity)
                        .hasDailyAmountSumEqualToWeeklyAmount());

        return this;
    }

    public TotalQuantityAssert hasWeeklyAmountSumEqualToTotalAmount() {
        isNotNull();

        int weeklyAmountSum = actual.getWeeklyQuantities()
                .stream()
                .mapToInt(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value())
                .sum();

        assertThat(actual.getTotalQuantityAmount().quantityAmount().value()).isEqualTo(weeklyAmountSum);

        return this;
    }
}
