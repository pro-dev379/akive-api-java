package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class QuantityMemoTest {

    @Test
    void 메모를_생성한다() {
        // when
        QuantityMemo quantityMemo = new QuantityMemo("메모");

        // then
        assertThat(quantityMemo.value()).isEqualTo("메모");
    }

    @Test
    void 메모는_null일_수_있다() {
        // when
        QuantityMemo quantityMemo = new QuantityMemo(null);

        // then
        assertThat(quantityMemo.value()).isNull();
    }

    @Test
    void 메모는_100자일_수_있다() {
        // given
        String value = "가".repeat(100);

        // when
        QuantityMemo quantityMemo = new QuantityMemo(value);

        // then
        assertThat(quantityMemo.value()).isEqualTo(value);
    }

    @Test
    void 메모는_100자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> new QuantityMemo("가".repeat(101)))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}