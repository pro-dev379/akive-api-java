package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import net.akive.api.plan.domain.exception.PlanException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NEGATIVE;
import static net.akive.api.plan.domain.exception.PlanErrorCode.DAILY_QUANTITY_AMOUNT_MISMATCH;
import static net.akive.api.plan.domain.exception.PlanErrorCode.DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT;
import static net.akive.api.plan.domain.exception.PlanErrorCode.DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyQuantityTest {

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
    private static final WeeklyQuantity VALID_WEEKLY_QUANTITY = WeeklyQuantity.create(
            2L,
            VALID_TOTAL_QUANTITY,
            new WeekPeriod(
                    LocalDate.of(2026, 7, 7),
                    LocalDate.of(2026, 7, 14)
            ),
            WeeklyQuantityAmount.of(10, null)
    );

    @Test
    void 하루_계획량을_생성한다() {
        // when
        DailyQuantity dailyQuantity = DailyQuantity.create(
                1L,
                VALID_WEEKLY_QUANTITY,
                LocalDate.of(2026, 7, 7),
                DailyQuantityAmount.of(10, 6, 4, "메모")
        );

        // then
        assertThat(dailyQuantity.getDailyQuantityId()).isEqualTo(1L);
        assertThat(dailyQuantity.getWeeklyQuantity()).isEqualTo(VALID_WEEKLY_QUANTITY);
        assertThat(dailyQuantity.getDate()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(dailyQuantity.getDailyQuantityAmount().quantityAmount().value()).isEqualTo(10);
        assertThat(dailyQuantity.getDailyQuantityAmount().completedQuantityAmount().value()).isEqualTo(6);
        assertThat(dailyQuantity.getDailyQuantityAmount().delayedQuantityAmount().value()).isEqualTo(4);
        assertThat(dailyQuantity.getDailyQuantityAmount().quantityMemo().value()).isEqualTo("메모");
    }

    @Test
    void 하루_계획량의_메모가_null이면_빈_메모_값_객체로_생성한다() {
        // when
        DailyQuantityAmount dailyQuantityAmount = DailyQuantityAmount.of(10, 6, 4, null);

        // then
        assertThat(dailyQuantityAmount.quantityMemo()).isNotNull();
        assertThat(dailyQuantityAmount.quantityMemo().value()).isNull();
    }

    @Test
    void 하루_계획량이_완수량과_딜레이량의_합과_같지_않으면_안_된다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantityAmount.of(10, 6, 3, null))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_AMOUNT_MISMATCH);
    }

    @Test
    void 하루_계획량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        DailyQuantityAmount.of(-1, 0, 0, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 하루_계획량은_주간_계획량보다_클_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        DailyQuantityAmount.of(11, 0, 11, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT);
    }

    @Test
    void 하루_계획량의_주간_계획량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        null,
                        LocalDate.of(2026, 7, 7),
                        DailyQuantityAmount.of(10, 0, 10, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량의_날짜는_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        null,
                        DailyQuantityAmount.of(10, 0, 10, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량의_날짜는_주간_계획량의_시작일보다_빠를_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 6),
                        DailyQuantityAmount.of(10, 0, 10, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD);
    }

    @Test
    void 하루_계획량의_날짜는_주간_계획량의_종료일보다_늦을_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 15),
                        DailyQuantityAmount.of(10, 0, 10, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD);
    }

    @Test
    void 하루_계획_완수량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        DailyQuantityAmount.of(10, -1, 0, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 하루_계획_딜레이량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        DailyQuantityAmount.of(10, 0, -1, null)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 하루_계획량의_메모는_25자를_초과할_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        DailyQuantityAmount.of(10, 0, 0, "가".repeat(26))
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
