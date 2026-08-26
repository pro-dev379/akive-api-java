package net.akive.api.plan.domain.model;

import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyQuantityTest {

    @Test
    void 저장된_주간_계획량을_복원한다() {
        Long weeklyQuantityId = 1L;
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100, null);
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        1L,
                        LocalDate.of(2026, 1, 1),
                        DailyQuantityAmount.of(100, 0, 100, null)
                )
        );

        WeeklyQuantity weeklyQuantity = WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                weekPeriod,
                weeklyQuantityAmount,
                dailyQuantities
        );

        assertThat(weeklyQuantity.getWeeklyQuantityId()).isEqualTo(weeklyQuantityId);
        assertThat(weeklyQuantity.getWeekPeriod()).isEqualTo(weekPeriod);
        assertThat(weeklyQuantity.getWeeklyQuantityAmount()).isEqualTo(weeklyQuantityAmount);
        assertThat(weeklyQuantity.getDailyQuantities()).containsExactlyElementsOf(dailyQuantities);
    }
}
