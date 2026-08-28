package net.akive.api.plan.domain.model;

import net.akive.api.plan.domain.value.DailyQuantityAmount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.plan.domain.PlanFixture.createSessionalPlan;
import static net.akive.api.plan.domain.PlanFixture.createSubject;
import static net.akive.api.plan.domain.PlanFixture.createTotalQuantityAmount;
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

    @Test
    void 같은_식별자를_가진_일일_계획량은_같게_취급한다() {
        DailyQuantity dailyQuantity = createDailyQuantity(1L);
        DailyQuantity sameDailyQuantity = createDailyQuantity(1L);

        assertThat(dailyQuantity).isEqualTo(sameDailyQuantity);
        assertThat(dailyQuantity.hashCode()).isEqualTo(sameDailyQuantity.hashCode());
    }

    @Test
    void 식별자가_다른_일일_계획량은_다르게_취급한다() {
        DailyQuantity dailyQuantity = createDailyQuantity(1L);
        DailyQuantity anotherDailyQuantity = createDailyQuantity(2L);

        assertThat(dailyQuantity).isNotEqualTo(anotherDailyQuantity);
    }

    @Test
    void 식별자가_없는_일일_계획량끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
        TotalQuantity totalQuantity = TotalQuantity.create(
                createSessionalPlan(),
                createSubject(),
                createTotalQuantityAmount()
        );
        TotalQuantity anotherTotalQuantity = TotalQuantity.create(
                createSessionalPlan(),
                createSubject(),
                createTotalQuantityAmount()
        );

        assertThat(totalQuantity.getWeeklyQuantities().getFirst().getDailyQuantities().getFirst())
                .isNotEqualTo(anotherTotalQuantity.getWeeklyQuantities().getFirst().getDailyQuantities().getFirst());
    }

    private DailyQuantity createDailyQuantity(Long dailyQuantityId) {
        return DailyQuantity.reconstruct(
                dailyQuantityId,
                LocalDate.of(2026, 1, 1),
                DailyQuantityAmount.of(100, 0, 100, null)
        );
    }
}
