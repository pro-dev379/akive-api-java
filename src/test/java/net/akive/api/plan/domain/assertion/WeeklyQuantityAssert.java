package net.akive.api.plan.domain.assertion;

import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class WeeklyQuantityAssert extends AbstractAssert<WeeklyQuantityAssert, WeeklyQuantity> {

    private WeeklyQuantityAssert(WeeklyQuantity actual) {
        super(actual, WeeklyQuantityAssert.class);
    }

    public static WeeklyQuantityAssert assertThatWeeklyQuantity(WeeklyQuantity actual) {
        return new WeeklyQuantityAssert(actual);
    }

    public WeeklyQuantityAssert hasAmount(int expectedAmount) {
        return hasAmount(WeeklyQuantityAmount.of(expectedAmount));
    }

    public WeeklyQuantityAssert hasAmount(WeeklyQuantityAmount expectedAmount) {
        isNotNull();

        assertThat(actual.getWeeklyQuantityAmount()).isEqualTo(expectedAmount);
        hasDailyAmountSumEqualToWeeklyAmount();

        return this;
    }

    public WeeklyQuantityAssert hasDailyAmounts(DailyQuantityAmount... expectedDailyAmounts) {
        isNotNull();

        assertThat(actual.getDailyQuantities())
                .extracting(DailyQuantity::getDailyQuantityAmount)
                .containsExactly(expectedDailyAmounts);

        return this;
    }

    public WeeklyQuantityAssert hasDailyAmountSumEqualToWeeklyAmount() {
        isNotNull();

        int dailyAmountSum = actual.getDailyQuantities()
                .stream()
                .map(DailyQuantity::getDailyQuantityAmount)
                .mapToInt(dailyQuantityAmount -> dailyQuantityAmount.quantityAmount().value())
                .sum();

        assertThat(actual.getWeeklyQuantityAmount().quantityAmount().value()).isEqualTo(dailyAmountSum);

        return this;
    }
}
