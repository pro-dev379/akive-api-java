package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import net.akive.api.plan.domain.exception.PlanException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NEGATIVE;
import static net.akive.api.plan.domain.exception.PlanErrorCode.WEEKLY_QUANTITY_AMOUNT_EXCEEDS_TOTAL_QUANTITY_AMOUNT;
import static net.akive.api.plan.domain.exception.PlanErrorCode.WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION_PERIOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeeklyQuantityTest {

    private static final SessionalPlan VALID_SESSIONAL_PLAN = SessionalPlan.create(
            2L,
            1L,
            "세션",
            new SessionPeriod(LocalDate.of(2026, 7, 7), LocalDate.of(2026, 7, 14))
    );
    private static final TotalQuantity VALID_TOTAL_QUANTITY = TotalQuantity.create(
            2L,
            VALID_SESSIONAL_PLAN,
            new Subject("수학", "과목"),
            TotalQuantityAmount.of(100, "문제", null)
    );
    private static final WeekPeriod VALID_WEEK_PERIOD = new WeekPeriod(
            LocalDate.of(2026, 7, 7),
            LocalDate.of(2026, 7, 14)
    );

    @Test
    void 주간_계획량을_생성한다() {
        // given
        String weeklyQuantityMemo = "메모";

        // when
        WeeklyQuantity weeklyQuantity = WeeklyQuantity.create(
                1L,
                VALID_TOTAL_QUANTITY,
                VALID_WEEK_PERIOD,
                WeeklyQuantityAmount.of(10, weeklyQuantityMemo)
        );

        // then
        assertThat(weeklyQuantity.getWeeklyQuantityId()).isEqualTo(1L);
        assertThat(weeklyQuantity.getTotalQuantity()).isEqualTo(VALID_TOTAL_QUANTITY);
        assertThat(weeklyQuantity.getWeekPeriod()).isEqualTo(VALID_WEEK_PERIOD);
        assertThat(weeklyQuantity.getWeeklyQuantityAmount().quantityAmount().value()).isEqualTo(10);
        assertThat(weeklyQuantity.getWeeklyQuantityAmount().quantityMemo().value()).isEqualTo(weeklyQuantityMemo);
    }

    @Test
    void 주간_계획량의_메모는_null이면_빈_메모_값_객체로_생성한다() {
        // when
        WeeklyQuantityAmount weeklyQuantityAmount = WeeklyQuantityAmount.of(10, null);

        // then
        assertThat(weeklyQuantityAmount.quantityMemo()).isNotNull();
        assertThat(weeklyQuantityAmount.quantityMemo().value()).isNull();
    }

    @Test
    void 주간_계획량의_세션_총량_식별자는_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        null,
                        VALID_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(10, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        VALID_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(-1, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 주간_계획량은_세션_총량보다_클_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        VALID_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(101, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(WEEKLY_QUANTITY_AMOUNT_EXCEEDS_TOTAL_QUANTITY_AMOUNT);
    }

    @Test
    void 주간_계획량의_기간은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        null,
                        WeeklyQuantityAmount.of(10, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획량의_기간은_세션_기간_시작일보다_빠를_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        new WeekPeriod(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 13)),
                        WeeklyQuantityAmount.of(10, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION_PERIOD);
    }

    @Test
    void 주간_계획량의_기간은_세션_기간_종료일보다_늦을_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        new WeekPeriod(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 15)),
                        WeeklyQuantityAmount.of(10, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION_PERIOD);
    }

    @Test
    void 주간_계획량의_메모는_25자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        VALID_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(10, "가".repeat(26))
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
