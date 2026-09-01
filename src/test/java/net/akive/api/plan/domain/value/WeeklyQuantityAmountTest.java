package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.VALUE_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeeklyQuantityAmountTest {

    @Test
    void 주간_계획량을_생성한다() {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100, "메모");

        assertThat(weeklyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(100));
        assertThat(weeklyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 메모가_없으면_빈_메모로_주간_계획량을_생성한다() {
        WeeklyQuantityAmount weeklyQuantityAmount = new WeeklyQuantityAmount(new QuantityAmount(100), null);

        assertThat(weeklyQuantityAmount.quantityMemo()).isEqualTo(QuantityMemo.NONE);
    }

    @Test
    void 주간_계획량을_증가시킨다() {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100, "메모");

        WeeklyQuantityAmount increasedWeeklyQuantityAmount = weeklyQuantityAmount.increaseBy(new QuantityAmount(20));

        assertThat(increasedWeeklyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(120));
        assertThat(increasedWeeklyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 주간_계획량을_감소시킨다() {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100, "메모");

        WeeklyQuantityAmount reducedWeeklyQuantityAmount = weeklyQuantityAmount.reduceBy(new QuantityAmount(20));

        assertThat(reducedWeeklyQuantityAmount.quantityAmount()).isEqualTo(new QuantityAmount(80));
        assertThat(reducedWeeklyQuantityAmount.quantityMemo()).isEqualTo(new QuantityMemo("메모"));
    }

    @Test
    void 주간_계획량을_0까지_감소시킬_수_있다() {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100);

        WeeklyQuantityAmount reducedWeeklyQuantityAmount = weeklyQuantityAmount.reduceBy(new QuantityAmount(100));

        assertThat(reducedWeeklyQuantityAmount.quantityAmount()).isEqualTo(QuantityAmount.ZERO);
    }

    @Test
    void 주간_계획량은_현재_계획량보다_많이_감소시킬_수_없다() {
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(100);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> weeklyQuantityAmount.reduceBy(new QuantityAmount(101)))
                .extracting(BadRequestException::getError)
                .isEqualTo(VALUE_NEGATIVE);
    }
}
