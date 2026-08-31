package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.exception.business.BadRequestError.VALUE_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class QuantityAmountTest {

    @Test
    void 계획량을_생성한다() {
        QuantityAmount quantityAmount = new QuantityAmount(10);

        assertThat(quantityAmount.value()).isEqualTo(10);
    }

    @Test
    void 계획량은_0일_수_있다() {
        QuantityAmount quantityAmount = new QuantityAmount(0);

        assertThat(quantityAmount).isEqualTo(QuantityAmount.ZERO);
    }

    @Test
    void 계획량은_음수일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new QuantityAmount(-1))
                .extracting(BadRequestException::getError)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 계획량을_더한다() {
        QuantityAmount quantityAmount = new QuantityAmount(10);

        QuantityAmount summedAmount = quantityAmount.plus(new QuantityAmount(5));

        assertThat(summedAmount).isEqualTo(new QuantityAmount(15));
    }

    @Test
    void 계획량을_뺀다() {
        QuantityAmount quantityAmount = new QuantityAmount(10);

        QuantityAmount subtractedAmount = quantityAmount.minus(new QuantityAmount(5));

        assertThat(subtractedAmount).isEqualTo(new QuantityAmount(5));
    }

    @Test
    void 뺄셈_결과가_음수이면_예외를_던진다() {
        QuantityAmount quantityAmount = new QuantityAmount(10);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> quantityAmount.minus(new QuantityAmount(15)))
                .extracting(BadRequestException::getError)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 두_계획량_중_작은_계획량을_반환한다() {
        QuantityAmount minAmount = QuantityAmount.min(
                new QuantityAmount(10),
                new QuantityAmount(5)
        );

        assertThat(minAmount).isEqualTo(new QuantityAmount(5));
    }

    @Test
    void 값이_0이면_계획량이_0인_계획량이다() {
        QuantityAmount quantityAmount = new QuantityAmount(0);

        assertThat(quantityAmount.isZero()).isTrue();
    }

    @Test
    void 값이_0보다_크면_계획량이_0인_것이_아니다() {
        QuantityAmount quantityAmount = new QuantityAmount(1);

        assertThat(quantityAmount.isZero()).isFalse();
    }

    @Test
    void 다른_계획량보다_작은지_확인한다() {
        QuantityAmount quantityAmount = new QuantityAmount(10);

        assertThat(quantityAmount.isLessThan(new QuantityAmount(20))).isTrue();
        assertThat(quantityAmount.isLessThan(new QuantityAmount(10))).isFalse();
        assertThat(quantityAmount.isLessThan(new QuantityAmount(5))).isFalse();
    }

    @Test
    void 변경될_계획량과의_변화량을_계산한다() {
        QuantityAmount quantityAmount = new QuantityAmount(10);

        QuantityAmountDelta quantityAmountDelta = quantityAmount.deltaTo(new QuantityAmount(20));

        assertThat(quantityAmountDelta).isEqualTo(new QuantityAmountDelta(10));
    }

    @Test
    void 계획량을_다른_계획량과_비교한다() {
        QuantityAmount quantityAmount = new QuantityAmount(10);

        assertThat(quantityAmount).isGreaterThan(new QuantityAmount(5));
        assertThat(quantityAmount).isEqualByComparingTo(new QuantityAmount(10));
        assertThat(quantityAmount).isLessThan(new QuantityAmount(20));
    }
}
