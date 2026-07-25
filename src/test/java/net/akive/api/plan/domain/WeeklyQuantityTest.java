package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.exception.business.BadRequestError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeeklyQuantityTest {

    private static final LocalDate VALID_SESSION_START_ON = LocalDate.of(2026, 7, 7);
    private static final LocalDate VALID_SESSION_END_ON = LocalDate.of(2026, 7, 14);
    private static final SessionPeriod VALID_SESSION_PERIOD = new SessionPeriod(
            VALID_SESSION_START_ON,
            VALID_SESSION_END_ON
    );
    private static final SessionalPlan VALID_SESSIONAL_PLAN = SessionalPlan.create(
            2L,
            1L,
            "세션",
            VALID_SESSION_PERIOD
    );
    private static final TotalQuantity VALID_TOTAL_QUANTITY = TotalQuantity.create(
            2L,
            VALID_SESSIONAL_PLAN,
            new Subject("수학", "과목"),
            TotalQuantityAmount.of(100, "문제", null)
    );
    private static final WeekPeriod VALID_WEEK_PERIOD = new WeekPeriod(
            VALID_SESSION_START_ON,
            VALID_SESSION_END_ON
    );
    private static final WeeklyQuantityAmount VALID_WEEKLY_QUANTITY_AMOUNT =
            WeeklyQuantityAmount.of(10, "메모");

    @Test
    void 주간_계획량을_식별자와_함께_생성한다() {
        // when
        WeeklyQuantity weeklyQuantity = WeeklyQuantity.create(
                1L,
                VALID_TOTAL_QUANTITY,
                VALID_WEEK_PERIOD,
                VALID_WEEKLY_QUANTITY_AMOUNT
        );

        // then
        assertThat(weeklyQuantity.getWeeklyQuantityId()).isEqualTo(1L);
        assertThat(weeklyQuantity.getTotalQuantity()).isEqualTo(VALID_TOTAL_QUANTITY);
        assertThat(weeklyQuantity.getWeekPeriod()).isEqualTo(VALID_WEEK_PERIOD);
        assertThat(weeklyQuantity.getWeeklyQuantityAmount()).isEqualTo(VALID_WEEKLY_QUANTITY_AMOUNT);
    }

    @Test
    void 세션_총량에_속한_주간_계획량을_식별자_없이_생성한다() {
        // when
        WeeklyQuantity weeklyQuantity = WeeklyQuantity.createForTotalQuantity(
                VALID_TOTAL_QUANTITY,
                VALID_SESSION_PERIOD,
                VALID_WEEK_PERIOD,
                VALID_WEEKLY_QUANTITY_AMOUNT
        );

        // then
        assertThat(weeklyQuantity.getWeeklyQuantityId()).isNull();
        assertThat(weeklyQuantity.getTotalQuantity()).isEqualTo(VALID_TOTAL_QUANTITY);
        assertThat(weeklyQuantity.getWeekPeriod()).isEqualTo(VALID_WEEK_PERIOD);
        assertThat(weeklyQuantity.getWeeklyQuantityAmount()).isEqualTo(VALID_WEEKLY_QUANTITY_AMOUNT);
    }

    @Test
    void 주간_계획량의_세션_총량은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        null,
                        VALID_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(10, null)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획량의_기간은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        null,
                        VALID_WEEKLY_QUANTITY_AMOUNT
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획량의_수량은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        VALID_WEEK_PERIOD,
                        null
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획량의_식별자는_양수여야_한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        0L,
                        VALID_TOTAL_QUANTITY,
                        VALID_WEEK_PERIOD,
                        VALID_WEEKLY_QUANTITY_AMOUNT
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(VALUE_NOT_POSITIVE);
    }

    @Test
    void 세션_총량에_속한_주간_계획량의_세션_기간은_null일_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.createForTotalQuantity(
                        VALID_TOTAL_QUANTITY,
                        null,
                        VALID_WEEK_PERIOD,
                        VALID_WEEKLY_QUANTITY_AMOUNT
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_계획량의_기간은_세션_기간_시작일과_종료일을_포함한다() {
        // when
        WeeklyQuantity weeklyQuantity = WeeklyQuantity.createForTotalQuantity(
                VALID_TOTAL_QUANTITY,
                VALID_SESSION_PERIOD,
                new WeekPeriod(VALID_SESSION_START_ON, VALID_SESSION_END_ON),
                VALID_WEEKLY_QUANTITY_AMOUNT
        );

        // then
        assertThat(weeklyQuantity.getWeekPeriod().startOn()).isEqualTo(VALID_SESSION_START_ON);
        assertThat(weeklyQuantity.getWeekPeriod().endOn()).isEqualTo(VALID_SESSION_END_ON);
    }

    @Test
    void 주간_계획량의_기간은_세션_기간_시작일보다_빠를_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.createForTotalQuantity(
                        VALID_TOTAL_QUANTITY,
                        VALID_SESSION_PERIOD,
                        new WeekPeriod(VALID_SESSION_START_ON.minusDays(1), VALID_SESSION_END_ON.minusDays(1)),
                        VALID_WEEKLY_QUANTITY_AMOUNT
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION);
    }

    @Test
    void 주간_계획량의_기간은_세션_기간_종료일보다_늦을_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.createForTotalQuantity(
                        VALID_TOTAL_QUANTITY,
                        VALID_SESSION_PERIOD,
                        new WeekPeriod(VALID_SESSION_START_ON.plusDays(1), VALID_SESSION_END_ON.plusDays(1)),
                        VALID_WEEKLY_QUANTITY_AMOUNT
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_QUANTITY_PERIOD_OUT_OF_SESSION);
    }

    @Test
    void 주간_계획량은_세션_총량보다_클_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.create(
                        1L,
                        VALID_TOTAL_QUANTITY,
                        VALID_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(101, null)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_QUANTITY_EXCEEDS_TOTAL_QUANTITY);
    }

    @Test
    void 세션_총량에_속한_주간_계획량은_세션_총량보다_클_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> WeeklyQuantity.createForTotalQuantity(
                        VALID_TOTAL_QUANTITY,
                        VALID_SESSION_PERIOD,
                        VALID_WEEK_PERIOD,
                        WeeklyQuantityAmount.of(101, null)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(WEEKLY_QUANTITY_EXCEEDS_TOTAL_QUANTITY);
    }
}
