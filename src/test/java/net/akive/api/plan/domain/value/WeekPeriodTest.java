package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.time.DayOfWeek.*;
import static net.akive.api.common.exception.business.BadRequestError.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WeekPeriodTest {

    @Test
    void 주차_기간을_생성한다() {
        LocalDate startOn = LocalDate.of(2026, 1, 5);
        LocalDate endOn = LocalDate.of(2026, 1, 11);

        WeekPeriod weekPeriod = new WeekPeriod(startOn, endOn);

        assertThat(weekPeriod.startOn()).isEqualTo(startOn);
        assertThat(weekPeriod.endOn()).isEqualTo(endOn);
    }

    @Test
    void 하루짜리_주차_기간을_생성한다() {
        LocalDate startOn = LocalDate.of(2026, 1, 1);
        LocalDate endOn = LocalDate.of(2026, 1, 1);

        WeekPeriod weekPeriod = new WeekPeriod(startOn, endOn);

        assertThat(weekPeriod.startOn()).isEqualTo(startOn);
        assertThat(weekPeriod.endOn()).isEqualTo(endOn);
    }

    @ParameterizedTest
    @MethodSource("provideNullDateWeekPeriods")
    void 시작일이나_종료일이_null이면_생성할_수_없다(LocalDate startOn, LocalDate endOn) {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeekPeriod(startOn, endOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(REQUIRED_VALUE_MISSING);
    }

    @Test
    void 시작일이_종료일보다_이후면_생성할_수_없다() {
        LocalDate startOn = LocalDate.of(2026, 1, 12);
        LocalDate endOn = LocalDate.of(2026, 1, 11);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeekPeriod(startOn, endOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_DATE_RANGE);
    }

    @Test
    void 기간이_7일을_초과하면_생성할_수_없다() {
        LocalDate startOn = LocalDate.of(2026, 1, 1);
        LocalDate endOn = LocalDate.of(2026, 1, 8);

        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> new WeekPeriod(startOn, endOn))
                .extracting(BadRequestException::getError)
                .isEqualTo(INVALID_WEEK_PERIOD);
    }

    @Test
    void 주차_기간을_날짜_목록으로_분할한다() {
        WeekPeriod weekPeriod = new WeekPeriod(
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 7)
        );

        List<PlanDate> planDates = weekPeriod.splitIntoDates();

        assertThat(planDates).containsExactly(
                new PlanDate(LocalDate.of(2026, 1, 5), MONDAY),
                new PlanDate(LocalDate.of(2026, 1, 6), TUESDAY),
                new PlanDate(LocalDate.of(2026, 1, 7), WEDNESDAY)
        );
    }

    private static Stream<Arguments> provideNullDateWeekPeriods() {
        return Stream.of(
                Arguments.arguments(null, LocalDate.of(2026, 1, 11)),
                Arguments.arguments(LocalDate.of(2026, 1, 5), null)
        );
    }
}
