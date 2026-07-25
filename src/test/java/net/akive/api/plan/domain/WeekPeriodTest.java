package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static java.time.DayOfWeek.*;
import static net.akive.api.common.exception.business.BadRequestError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeekPeriodTest {

    @Test
    void 주간_기간을_생성한다() {
        // given
        LocalDate startOn = LocalDate.of(2026, 7, 7);
        LocalDate endOn = LocalDate.of(2026, 7, 14);

        // when
        WeekPeriod weekPeriod = new WeekPeriod(startOn, endOn);

        // then
        assertThat(weekPeriod.startOn()).isEqualTo(startOn);
        assertThat(weekPeriod.endOn()).isEqualTo(endOn);
    }

    @Test
    void 시작일이_null이면_주간_기간_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeekPeriod(null, LocalDate.of(2026, 7, 14)))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 종료일이_null이면_주간_기간_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeekPeriod(LocalDate.of(2026, 7, 7), null))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 주간_기간은_7일을_초과할_수_없다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeekPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 15)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_WEEK_PERIOD);
    }

    @Test
    void 주간_기간은_7일보다_짧을_수_있다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 13)
        );

        assertThat(weekPeriod.startOn()).isEqualTo(LocalDate.of(2026, 7, 7));
        assertThat(weekPeriod.endOn()).isEqualTo(LocalDate.of(2026, 7, 13));
    }

    @Test
    void 주간_기간을_날짜와_요일_목록으로_나눈다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2024, 3, 25),
                LocalDate.of(2024, 3, 31)
        );

        List<PlanDate> planDates = weekPeriod.splitIntoDates();

        assertThat(planDates).containsExactly(
                new PlanDate(LocalDate.of(2024, 3, 25), MONDAY),
                new PlanDate(LocalDate.of(2024, 3, 26), TUESDAY),
                new PlanDate(LocalDate.of(2024, 3, 27), WEDNESDAY),
                new PlanDate(LocalDate.of(2024, 3, 28), THURSDAY),
                new PlanDate(LocalDate.of(2024, 3, 29), FRIDAY),
                new PlanDate(LocalDate.of(2024, 3, 30), SATURDAY),
                new PlanDate(LocalDate.of(2024, 3, 31), SUNDAY)
        );
    }

    @Test
    void 시작일이_종료일보다_이후이면_주간_기간_생성에_실패한다() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeekPeriod(
                        LocalDate.of(2026, 7, 13),
                        LocalDate.of(2026, 7, 12)
                ))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_DATE_RANGE);
    }

    @Test
    void 시작일과_종료일이_같으면_같은_주간_기간이다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 14)
        );

        assertThat(weekPeriod).isNotEqualTo(
                new WeekPeriod(
                        LocalDate.of(2026, 7, 8),
                        LocalDate.of(2026, 7, 14)
                )
        );
        assertThat(weekPeriod).isEqualTo(
                new WeekPeriod(
                        LocalDate.of(2026, 7, 7),
                        LocalDate.of(2026, 7, 14)
                )
        );
    }
}
