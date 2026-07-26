package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;

import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.exception.business.BadRequestError.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.exception.business.BadRequestError.VALUE_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeeklyQuantityAmountTest {

    @Test
    void 주간_계획량을_생성한다() {
        // when
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(10, "메모");

        // then
        assertThat(weeklyQuantityAmount.quantityAmount().value()).isEqualTo(10);
        assertThat(weeklyQuantityAmount.quantityMemo().value()).isEqualTo("메모");
    }

    @Test
    void 주간_계획량은_0일_수_있다() {
        // when
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(0, null);

        // then
        assertThat(weeklyQuantityAmount.quantityAmount().value()).isZero();
    }

    @Test
    void 주간_계획량은_음수일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantityAmount.of(-1, null))
                .extracting(BadRequestException::getError)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 주간_계획량의_수량은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeeklyQuantityAmount(null, null))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 메모는_null이면_빈_메모_값_객체로_생성한다() {
        // when
        WeeklyQuantityAmount weeklyQuantityAmount = new WeeklyQuantityAmount(new QuantityAmount(10), null);

        // then
        assertThat(weeklyQuantityAmount.quantityMemo()).isNotNull();
        assertThat(weeklyQuantityAmount.quantityMemo().value()).isNull();
    }

    @Test
    void 메모는_100자를_초과할_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantityAmount.of(10, "가".repeat(101)))
                .extracting(BadRequestException::getError)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
