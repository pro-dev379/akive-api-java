package net.akive.api.plan.domain;

import net.akive.api.plan.domain.model.DailyQuantity;
import net.akive.api.plan.domain.model.TotalQuantity;
import net.akive.api.plan.domain.model.WeeklyQuantity;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PlanAssertions {

    private PlanAssertions() {
    }

    public static void assertWeeklyQuantityAmounts(TotalQuantity totalQuantity, int... expectedAmounts) {
        assertThat(totalQuantity.getWeeklyQuantities())
                .extracting(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount()
                        .quantityAmount()
                        .value())
                .containsExactlyElementsOf(Arrays.stream(expectedAmounts).boxed().toList());

        assertTotalAmountMatchesWeeklyAmounts(totalQuantity);
        totalQuantity.getWeeklyQuantities().forEach(PlanAssertions::assertWeeklyAmountMatchesDailyAmounts);
    }

    private static void assertTotalAmountMatchesWeeklyAmounts(TotalQuantity totalQuantity) {
        int weeklyAmountSum = totalQuantity.getWeeklyQuantities()
                .stream()
                .mapToInt(weeklyQuantity -> weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value())
                .sum();

        assertThat(totalQuantity.getTotalQuantityAmount().quantityAmount().value()).isEqualTo(weeklyAmountSum);
    }

    private static void assertWeeklyAmountMatchesDailyAmounts(WeeklyQuantity weeklyQuantity) {
        int dailyAmountSum = weeklyQuantity.getDailyQuantities()
                .stream()
                .map(DailyQuantity::getDailyQuantityAmount)
                .mapToInt(dailyQuantityAmount -> dailyQuantityAmount.quantityAmount().value())
                .sum();

        assertThat(weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value()).isEqualTo(dailyAmountSum);
    }
}
