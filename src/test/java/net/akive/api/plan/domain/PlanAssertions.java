package net.akive.api.plan.domain;

import net.akive.api.plan.domain.model.TotalQuantity;

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
    }
}
