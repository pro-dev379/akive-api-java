package net.akive.api.plan.domain.model;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.plan.domain.value.DailyQuantityAmount;
import net.akive.api.plan.domain.value.WeekPeriod;
import net.akive.api.plan.domain.value.WeeklyQuantityAmount;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.exception.business.BadRequestError.DAILY_QUANTITY_DATE_OUT_OF_WEEK;
import static net.akive.api.common.exception.business.BadRequestError.WEEKLY_QUANTITY_AMOUNT_MISMATCH;
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

    @Test
    void 일일_계획량_합계가_주간_계획량과_다르면_저장된_주간_계획량을_복원할_수_없다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100, null);
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        1L,
                        LocalDate.of(2026, 1, 1),
                        DailyQuantityAmount.of(60, 0, 60, null)
                )
        );

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.reconstruct(
                        1L,
                        weekPeriod,
                        weeklyQuantityAmount,
                        dailyQuantities
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_QUANTITY_AMOUNT_MISMATCH);
    }

    @Test
    void 일일_계획량_날짜가_주간_기간을_벗어나면_저장된_주간_계획량을_복원할_수_없다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 4)
        );
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100, null);
        List<DailyQuantity> dailyQuantities = List.of(
                DailyQuantity.reconstruct(
                        1L,
                        LocalDate.of(2026, 1, 5),
                        DailyQuantityAmount.of(100, 0, 100, null)
                )
        );

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.reconstruct(
                        1L,
                        weekPeriod,
                        weeklyQuantityAmount,
                        dailyQuantities
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(DAILY_QUANTITY_DATE_OUT_OF_WEEK);
    }
}
