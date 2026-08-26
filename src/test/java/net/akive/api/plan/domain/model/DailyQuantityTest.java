package net.akive.api.plan.domain.model;

import net.akive.api.plan.domain.value.DailyQuantityAmount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DailyQuantityTest {

    @Test
    void 저장된_일일_계획량을_복원한다() {
        Long dailyQuantityId = 1L;
        LocalDate date = LocalDate.of(2026, 1, 1);
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(100, 0, 100, null);

        DailyQuantity dailyQuantity = DailyQuantity.reconstruct(
                dailyQuantityId,
                date,
                dailyQuantityAmount
        );

        assertThat(dailyQuantity.getDailyQuantityId()).isEqualTo(dailyQuantityId);
        assertThat(dailyQuantity.getDate()).isEqualTo(date);
        assertThat(dailyQuantity.getDailyQuantityAmount()).isEqualTo(dailyQuantityAmount);
    }
}
