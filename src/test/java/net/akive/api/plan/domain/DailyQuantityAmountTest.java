package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import net.akive.api.plan.domain.exception.PlanException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NEGATIVE;
import static net.akive.api.plan.domain.exception.PlanErrorCode.DAILY_QUANTITY_AMOUNT_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyQuantityAmountTest {

    @Test
    void 하루_계획량을_생성한다() {
        // when
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(10, 6, 4, "메모");

        // then
        assertThat(dailyQuantityAmount.quantityAmount().value()).isEqualTo(10);
        assertThat(dailyQuantityAmount.completedQuantityAmount().value()).isEqualTo(6);
        assertThat(dailyQuantityAmount.delayedQuantityAmount().value()).isEqualTo(4);
        assertThat(dailyQuantityAmount.quantityMemo().value()).isEqualTo("메모");
    }

    @Test
    void 하루_계획량은_0일_수_있다() {
        // when
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(0, 0, 0, null);

        // then
        assertThat(dailyQuantityAmount.quantityAmount().value()).isZero();
        assertThat(dailyQuantityAmount.completedQuantityAmount().value()).isZero();
        assertThat(dailyQuantityAmount.delayedQuantityAmount().value()).isZero();
    }

    @Test
    void 하루_계획량의_수량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new DailyQuantityAmount(null, new QuantityAmount(0), new QuantityAmount(0), null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량의_완수량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new DailyQuantityAmount(new QuantityAmount(0), null, new QuantityAmount(0), null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량의_딜레이량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new DailyQuantityAmount(new QuantityAmount(0), new QuantityAmount(0), null, null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantityAmount.of(-1, 0, 0, null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 하루_계획_완수량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantityAmount.of(10, -1, 11, null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 하루_계획_딜레이량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantityAmount.of(10, 11, -1, null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 하루_계획량은_완수량과_딜레이량의_합과_같아야_한다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantityAmount.of(10, 6, 3, null))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_AMOUNT_MISMATCH);
    }

    @Test
    void 메모는_null이면_빈_메모_값_객체로_생성한다() {
        // when
        DailyQuantityAmount dailyQuantityAmount = new DailyQuantityAmount(
                new QuantityAmount(10),
                new QuantityAmount(6),
                new QuantityAmount(4),
                null
        );

        // then
        assertThat(dailyQuantityAmount.quantityMemo()).isNotNull();
        assertThat(dailyQuantityAmount.quantityMemo().value()).isNull();
    }

    @Test
    void 메모는_25자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantityAmount.of(10, 6, 4, "가".repeat(26)))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}