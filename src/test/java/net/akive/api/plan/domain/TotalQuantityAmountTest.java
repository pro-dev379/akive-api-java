package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.BLANK_VALUE;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TotalQuantityAmountTest {

    @Test
    void 전체_계획량을_생성한다() {
        // when
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(100, "문제", "메모");

        // then
        assertThat(totalQuantityAmount.quantityAmount().value()).isEqualTo(100);
        assertThat(totalQuantityAmount.unit()).isEqualTo("문제");
        assertThat(totalQuantityAmount.quantityMemo().value()).isEqualTo("메모");
    }

    @Test
    void 전체_계획량은_0일_수_있다() {
        // when
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(0, "문제", null);

        // then
        assertThat(totalQuantityAmount.quantityAmount().value()).isZero();
    }

    @Test
    void 전체_계획량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantityAmount.of(-1, "문제", null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 전체_계획량의_수량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new TotalQuantityAmount(null, "문제", null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 단위는_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantityAmount.of(100, null, null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 단위는_공백일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantityAmount.of(100, " ", null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(BLANK_VALUE);
    }

    @Test
    void 단위는_앞뒤_공백을_제거한다() {
        // when
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(100, "  문제  ", null);

        // then
        assertThat(totalQuantityAmount.unit()).isEqualTo("문제");
    }

    @Test
    void 단위는_10자일_수_있다() {
        // given
        String unit = "가".repeat(10);

        // when
        TotalQuantityAmount totalQuantityAmount = TotalQuantityAmount.of(100, unit, null);

        // then
        assertThat(totalQuantityAmount.unit()).isEqualTo(unit);
    }

    @Test
    void 단위는_10자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantityAmount.of(100, "가".repeat(11), null))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }

    @Test
    void 메모는_null이면_빈_메모_값_객체로_생성한다() {
        // when
        TotalQuantityAmount totalQuantityAmount = new TotalQuantityAmount(new QuantityAmount(100), "문제", null);

        // then
        assertThat(totalQuantityAmount.quantityMemo()).isNotNull();
        assertThat(totalQuantityAmount.quantityMemo().value()).isNull();
    }

    @Test
    void 메모는_100자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> TotalQuantityAmount.of(100, "문제", "가".repeat(101)))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}