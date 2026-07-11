package net.akive.api.plan.domain;

import net.akive.api.common.domain.exception.DomainValidationException;
import net.akive.api.plan.domain.exception.DailyQuantityException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.akive.api.common.domain.exception.DomainValidationErrorCode.MAX_LENGTH_EXCEEDED;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.REQUIRED_VALUE_MISSING;
import static net.akive.api.common.domain.exception.DomainValidationErrorCode.VALUE_NEGATIVE;
import static net.akive.api.plan.domain.exception.DailyQuantityErrorCode.AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT;
import static net.akive.api.plan.domain.exception.DailyQuantityErrorCode.DATE_OUT_OF_WEEK_PERIOD;
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
            "수학",
            "과목",
            100,
            "문제",
            null
    );
    private static final WeeklyQuantity VALID_WEEKLY_QUANTITY = WeeklyQuantity.create(
            2L,
            VALID_TOTAL_QUANTITY,
            new WeekPeriod(
                    LocalDate.of(2026, 7, 7),
                    LocalDate.of(2026, 7, 14)
            ),
            10,
            null
    );

    @Test
    void 하루_계획량을_생성한다() {
        // when
        DailyQuantity dailyQuantity = DailyQuantity.create(
                1L,
                VALID_WEEKLY_QUANTITY,
                LocalDate.of(2026, 7, 7),
                10,
                6,
                1,
                "메모"
        );

        // then
        assertThat(dailyQuantity.getDailyQuantityId()).isEqualTo(1L);
        assertThat(dailyQuantity.getWeeklyQuantity()).isEqualTo(VALID_WEEKLY_QUANTITY);
        assertThat(dailyQuantity.getDate()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(dailyQuantity.getDailyQuantityAmount()).isEqualTo(10);
        assertThat(dailyQuantity.getCompletedQuantityAmount()).isEqualTo(6);
        assertThat(dailyQuantity.getDelayQuantityAmount()).isEqualTo(1);
        assertThat(dailyQuantity.getDailyQuantityMemo()).isEqualTo("메모");
    }

    @Test
    void 하루_계획량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        -1,
                        0,
                        0,
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(VALUE_NEGATIVE);
    }

    @Test
    void 하루_계획량은_주간_계획량보다_클_수_없다() {
        assertThatExceptionOfType(DailyQuantityException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        11,
                        0,
                        0,
                        null
                ))
                .extracting(DailyQuantityException::getErrorCode)
                .isEqualTo(AMOUNT_EXCEEDS_WEEKLY_QUANTITY_AMOUNT);
    }

    @Test
    void 하루_계획량의_주간_계획량은_null일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        null,
                        LocalDate.of(2026, 7, 7),
                        10,
                        0,
                        0,
                        null
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
                        10,
                        0,
                        0,
                        null
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 하루_계획량의_날짜는_주간_계획량의_시작일보다_빠를_수_없다() {
        assertThatExceptionOfType(DailyQuantityException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 6),
                        10,
                        0,
                        0,
                        null
                ))
                .extracting(DailyQuantityException::getErrorCode)
                .isEqualTo(DATE_OUT_OF_WEEK_PERIOD);
    }

    @Test
    void 하루_계획량의_날짜는_주간_계획량의_종료일보다_늦을_수_없다() {
        assertThatExceptionOfType(DailyQuantityException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 15),
                        10,
                        0,
                        0,
                        null
                ))
                .extracting(DailyQuantityException::getErrorCode)
                .isEqualTo(DATE_OUT_OF_WEEK_PERIOD);
    }

    @Test
    void 하루_계획_완수량은_음수일_수_없다() {
        assertThatExceptionOfType(DomainValidationException.class)
                .isThrownBy(() -> DailyQuantity.create(
                        1L,
                        VALID_WEEKLY_QUANTITY,
                        LocalDate.of(2026, 7, 7),
                        10,
                        -1,
                        0,
                        null
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
                        10,
                        0,
                        -1,
                        null
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
                        10,
                        0,
                        0,
                        "가".repeat(26)
                ))
                .extracting(DomainValidationException::getErrorCode)
                .isEqualTo(MAX_LENGTH_EXCEEDED);
    }
}
