package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import net.akive.api.plan.exception.PlanException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NOT_POSITIVE;
import static net.akive.api.plan.exception.PlanErrorCode.DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT;
import static net.akive.api.plan.exception.PlanErrorCode.DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DailyQuantityTest {

    private static final LocalDate VALID_WEEK_START_ON = LocalDate.of(2026, 7, 7);
    private static final LocalDate VALID_WEEK_END_ON = LocalDate.of(2026, 7, 14);
    private static final SessionalPlan VALID_SESSIONAL_PLAN = SessionalPlan.create(
            2L,
            1L,
            "세션",
            new SessionPeriod(VALID_WEEK_START_ON, VALID_WEEK_END_ON)
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
            new WeekPeriod(VALID_WEEK_START_ON, VALID_WEEK_END_ON),
            WeeklyQuantityAmount.of(10, null)
    );
    private static final DailyQuantityAmount VALID_DAILY_QUANTITY_AMOUNT =
            DailyQuantityAmount.of(10, 6, 4, "메모");

    @Test
    void 하루_계획량을_생성한다() {
        // when
        DailyQuantity dailyQuantity = DailyQuantity.create(
                1L,
                VALID_WEEKLY_QUANTITY,
                VALID_WEEK_START_ON,
                VALID_DAILY_QUANTITY_AMOUNT
        );

        // then
        assertThat(dailyQuantity.getDailyQuantityId()).isEqualTo(1L);
        assertThat(dailyQuantity.getWeeklyQuantity()).isEqualTo(VALID_WEEKLY_QUANTITY);
        assertThat(dailyQuantity.getDate()).isEqualTo(VALID_WEEK_START_ON);
        assertThat(dailyQuantity.getDailyQuantityAmount().quantityAmount().value()).isEqualTo(10);
        assertThat(dailyQuantity.getDailyQuantityAmount().completedQuantityAmount().value()).isEqualTo(6);
        assertThat(dailyQuantity.getDailyQuantityAmount().delayedQuantityAmount().value()).isEqualTo(4);
        assertThat(dailyQuantity.getDailyQuantityAmount().quantityMemo().value()).isEqualTo("메모");
    }

    @Test
    void 주간_계획량에_속한_하루_계획량을_생성한다() {
        // when
        DailyQuantity dailyQuantity = DailyQuantity.createForWeeklyQuantity(
                VALID_WEEKLY_QUANTITY,
                VALID_WEEK_START_ON.plusDays(1),
                VALID_DAILY_QUANTITY_AMOUNT
        );

        // then
        assertThat(dailyQuantity.getDailyQuantityId()).isNull();
        assertThat(dailyQuantity.getWeeklyQuantity()).isEqualTo(VALID_WEEKLY_QUANTITY);
        assertThat(dailyQuantity.getDate()).isEqualTo(VALID_WEEK_START_ON.plusDays(1));
        assertThat(dailyQuantity.getDailyQuantityAmount()).isEqualTo(VALID_DAILY_QUANTITY_AMOUNT);
    }

    @Test
    void 하루_계획량의_주간_계획량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        null,
                        VALID_WEEK_START_ON,
                        VALID_DAILY_QUANTITY_AMOUNT
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
                        VALID_DAILY_QUANTITY_AMOUNT
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량의_수량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        VALID_WEEK_START_ON,
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량의_식별자는_양수여야_한다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        0L,
                        VALID_WEEKLY_QUANTITY,
                        VALID_WEEK_START_ON,
                        VALID_DAILY_QUANTITY_AMOUNT
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 하루_계획량의_날짜는_주간_계획량_시작일과_종료일을_포함한다() {
        // when
        DailyQuantity startDateDailyQuantity = DailyQuantity.create(
                1L,
                VALID_WEEKLY_QUANTITY,
                VALID_WEEK_START_ON,
                VALID_DAILY_QUANTITY_AMOUNT
        );
        DailyQuantity endDateDailyQuantity = DailyQuantity.create(
                2L,
                VALID_WEEKLY_QUANTITY,
                VALID_WEEK_END_ON,
                VALID_DAILY_QUANTITY_AMOUNT
        );

        // then
        assertThat(startDateDailyQuantity.getDate()).isEqualTo(VALID_WEEK_START_ON);
        assertThat(endDateDailyQuantity.getDate()).isEqualTo(VALID_WEEK_END_ON);
    }

    @Test
    void 하루_계획량의_날짜는_주간_계획량의_시작일보다_빠를_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        VALID_WEEK_START_ON.minusDays(1),
                        VALID_DAILY_QUANTITY_AMOUNT
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
                        VALID_WEEK_END_ON.plusDays(1),
                        VALID_DAILY_QUANTITY_AMOUNT
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_DATE_OUT_OF_WEEK_PERIOD);
    }

    @Test
    void 하루_계획량은_주간_계획량보다_클_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        VALID_WEEK_START_ON,
                        DailyQuantityAmount.of(11, 0, 11, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT);
    }

    @Test
    void 주간_계획량에_속한_하루_계획량은_주간_계획량보다_클_수_없다() {
        assertThatExceptionOfType(PlanException.class)
                .isThrownBy(() -> DailyQuantity.createForWeeklyQuantity(
                        VALID_WEEKLY_QUANTITY,
                        VALID_WEEK_START_ON,
                        DailyQuantityAmount.of(11, 0, 11, null)
                ))
                .extracting(PlanException::getErrorCode)
                .isEqualTo(DAILY_QUANTITY_AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT);
    }
}
