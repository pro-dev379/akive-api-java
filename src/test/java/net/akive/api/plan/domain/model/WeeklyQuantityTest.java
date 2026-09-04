package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BusinessRuleViolationException;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BusinessRuleViolationError.DAILY_QUANTITY_DATE_OUT_OF_WEEK;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.WEEKLY_AMOUNT_MISMATCH;
import static net.akive.api.plan.domain.PlanFixture.createSessionalPlan;
import static net.akive.api.plan.domain.PlanFixture.createSubject;
import static net.akive.api.plan.domain.PlanFixture.createTotalQuantityAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeeklyQuantityTest {

    @Test
    void 저장된_주간_계획량을_복원한다() {
        Long weeklyQuantityId = 1L;
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100);
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        1L,
                        LocalDate.of(2026, 1, 1),
                        DailyQuantityAmount.of(100, 0, 100)
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

    @Test
    void 일일_계획량_합계가_주간_계획량과_다르면_저장된_주간_계획량을_복원할_수_없다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100);
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        1L,
                        LocalDate.of(2026, 1, 1),
                        DailyQuantityAmount.of(60, 0, 60)
                )
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> WeeklyQuantity.reconstruct(
                        1L,
                        weekPeriod,
                        weeklyQuantityAmount,
                        dailyQuantities
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(WEEKLY_AMOUNT_MISMATCH);
    }

    @Test
    void 일일_계획량_날짜가_주간_기간을_벗어나면_저장된_주간_계획량을_복원할_수_없다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100);
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        1L,
                        LocalDate.of(2026, 1, 5),
                        DailyQuantityAmount.of(100, 0, 100)
                )
        );

        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> WeeklyQuantity.reconstruct(
                        1L,
                        weekPeriod,
                        weeklyQuantityAmount,
                        dailyQuantities
                ))
                .extracting(BusinessRuleViolationException::getError)
                .isEqualTo(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
    }

    @Test
    void 같은_식별자를_가진_주간_계획량은_같게_취급한다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(1L);
        WeeklyQuantity sameWeeklyQuantity = createWeeklyQuantity(1L);

        assertThat(weeklyQuantity).isEqualTo(sameWeeklyQuantity);
        assertThat(weeklyQuantity.hashCode()).isEqualTo(sameWeeklyQuantity.hashCode());
    }

    @Test
    void 식별자가_다른_주간_계획량은_다르게_취급한다() {
        WeeklyQuantity weeklyQuantity = createWeeklyQuantity(1L);
        WeeklyQuantity anotherWeeklyQuantity = createWeeklyQuantity(2L);

        assertThat(weeklyQuantity).isNotEqualTo(anotherWeeklyQuantity);
    }

    @Test
    void 식별자가_없는_주간_계획량끼리는_나머지_값이_같더라도_다른_것으로_취급한다() {
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

        assertThat(totalQuantity.getWeeklyQuantities().getFirst())
                .isNotEqualTo(anotherTotalQuantity.getWeeklyQuantities().getFirst());
    }

    private WeeklyQuantity createWeeklyQuantity(Long weeklyQuantityId) {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );

        return WeeklyQuantity.reconstruct(
                weeklyQuantityId,
                weekPeriod,
                WeeklyQuantityAmount.of(100),
                List.of(DailyQuantity.reconstruct(
                        1L,
                        LocalDate.of(2026, 1, 1),
                        DailyQuantityAmount.of(100, 0, 100)
                ))
        );
    }
}
