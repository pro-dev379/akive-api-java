package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NEGATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class QuantityAmountTest {

    @Test
    void 수량을_생성한다() {
        // when
        QuantityAmount quantityAmount = new QuantityAmount(10);

        // then
        assertThat(quantityAmount.value()).isEqualTo(10);
    }

    @Test
    void 수량은_0일_수_있다() {
        // when
        QuantityAmount quantityAmount = new QuantityAmount(0);

        // then
        assertThat(quantityAmount.value()).isZero();
    }

    @Test
    void 수량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new QuantityAmount(-1))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }
}