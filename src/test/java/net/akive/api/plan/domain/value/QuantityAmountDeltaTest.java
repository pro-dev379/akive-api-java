package net.akive.api.plan.domain.value;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuantityAmountDeltaTest {

    @Test
    void 계획량_변경_이전과_이후간의_변화량을_계산한다() {
        QuantityAmount before = new QuantityAmount(100);
        QuantityAmount after = new QuantityAmount(70);

        QuantityAmountDelta quantityAmountDelta = QuantityAmountDelta.calculate(before, after);

        assertThat(quantityAmountDelta).isEqualTo(new QuantityAmountDelta(-30));
    }

    @Test
    void 계획량_변화분에_절대값을_적용해서_계획량으로_변환한다() {
        QuantityAmountDelta quantityAmountDelta = new QuantityAmountDelta(-30);

        QuantityAmount absoluteAmount = quantityAmountDelta.toAbsoluteAmount();

        assertThat(absoluteAmount).isEqualTo(new QuantityAmount(30));
    }

    @Test
    void 계획량_변경분이_0보다_크면_양의_변화량이다() {
        QuantityAmountDelta quantityAmountDelta = new QuantityAmountDelta(30);

        assertThat(quantityAmountDelta.isPositive()).isTrue();
        assertThat(quantityAmountDelta.isNegative()).isFalse();
        assertThat(quantityAmountDelta.isZero()).isFalse();
    }

    @Test
    void 계획량_변경분이_0보다_작으면_음의_변화량이다() {
        QuantityAmountDelta quantityAmountDelta = new QuantityAmountDelta(-30);

        assertThat(quantityAmountDelta.isPositive()).isFalse();
        assertThat(quantityAmountDelta.isNegative()).isTrue();
        assertThat(quantityAmountDelta.isZero()).isFalse();
    }

    @Test
    void 계획량_변경분이_0이면_변화량이_없는_것이다() {
        QuantityAmountDelta quantityAmountDelta = new QuantityAmountDelta(0);

        assertThat(quantityAmountDelta.isPositive()).isFalse();
        assertThat(quantityAmountDelta.isNegative()).isFalse();
        assertThat(quantityAmountDelta.isZero()).isTrue();
    }

    @Test
    void 다른_변화량과_비교한다() {
        QuantityAmountDelta quantityAmountDelta = new QuantityAmountDelta(10);

        assertThat(quantityAmountDelta).isGreaterThan(new QuantityAmountDelta(-10));
        assertThat(quantityAmountDelta).isEqualByComparingTo(new QuantityAmountDelta(10));
        assertThat(quantityAmountDelta).isLessThan(new QuantityAmountDelta(20));
    }
}
