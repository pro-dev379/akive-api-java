package net.akive.api.dailylog.domain;

import net.akive.api.user.domain.GoalVersion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static net.akive.api.common.analytics.TimeSeriesUnit.DAY;
import static net.akive.api.common.analytics.TimeSeriesUnit.MONTH;
import static net.akive.api.common.analytics.TimeSeriesUnit.WEEK;
import static net.akive.api.common.time.ServiceDayPolicy.toServiceDayStart;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.LOG_ON;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.hours;
import static net.akive.api.dailylog.domain.fixture.DailyLogFixture.study;
import static net.akive.api.user.domain.fixture.GoalFixture.createGoalVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.groups.Tuple.tuple;

class FocusMetricsSeriesTest {

    @Test
    void 일_단위로_각_날짜의_집중_시간과_집중도를_계산한다() {
        DailyLog firstDayStudy = study(LOG_ON, 9, 11, hours(1));
        DailyLog secondDayStudy = study(LOG_ON.plusDays(1), 9, 17, hours(6));
        GoalVersion goal = createGoalVersion(toServiceDayStart(LOG_ON.minusDays(1)));

        FocusMetricsSeries focusMetricsSeries = FocusMetricsSeries.calculate(
                LOG_ON,
                LOG_ON.plusDays(1),
                DAY,
                List.of(firstDayStudy, secondDayStudy),
                List.of(goal)
        );

        assertThat(focusMetricsSeries.periodFocusMetrics()).hasSize(2);
        assertSoftly(softly -> {
            FocusMetricsSeries.PeriodFocusMetrics firstDay = focusMetricsSeries.periodFocusMetrics().getFirst();
            softly.assertThat(firstDay.startOn()).isEqualTo(LOG_ON);
            softly.assertThat(firstDay.endOn()).isEqualTo(LOG_ON);
            softly.assertThat(firstDay.focusMetrics().focusTimeSec()).isEqualTo(hours(1));
            softly.assertThat(firstDay.focusMetrics().focusRate()).isEqualByComparingTo("50.0");

            FocusMetricsSeries.PeriodFocusMetrics secondDay = focusMetricsSeries.periodFocusMetrics().get(1);
            softly.assertThat(secondDay.startOn()).isEqualTo(LOG_ON.plusDays(1));
            softly.assertThat(secondDay.endOn()).isEqualTo(LOG_ON.plusDays(1));
            softly.assertThat(secondDay.focusMetrics().focusTimeSec()).isEqualTo(hours(6));
            softly.assertThat(secondDay.focusMetrics().focusRate()).isEqualByComparingTo("75.0");
        });
    }

    @Test
    void 주_단위는_일요일을_종료일로_구분하고_첫째와_마지막_주에는_조회_기간에_포함된_날짜만_집계한다() {
        LocalDate startOn = LocalDate.of(2026, 9, 16);
        LocalDate endOn = LocalDate.of(2026, 9, 22);

        FocusMetricsSeries focusMetricsSeries = FocusMetricsSeries.calculate(
                startOn,
                endOn,
                WEEK,
                List.of(),
                List.of()
        );

        assertThat(focusMetricsSeries.periodFocusMetrics())
                .extracting(
                        FocusMetricsSeries.PeriodFocusMetrics::startOn,
                        FocusMetricsSeries.PeriodFocusMetrics::endOn
                )
                .containsExactly(
                        tuple(startOn, LocalDate.of(2026, 9, 20)),
                        tuple(LocalDate.of(2026, 9, 21), endOn)
                );
    }

    @Test
    void 월_단위는_월말을_종료일로_구분하고_첫째와_마지막_달에는_조회_기간에_포함된_날짜만_집계한다() {
        LocalDate startOn = LocalDate.of(2026, 9, 14);
        LocalDate endOn = LocalDate.of(2026, 10, 3);

        FocusMetricsSeries focusMetricsSeries = FocusMetricsSeries.calculate(
                startOn,
                endOn,
                MONTH,
                List.of(),
                List.of()
        );

        assertThat(focusMetricsSeries.periodFocusMetrics())
                .extracting(
                        FocusMetricsSeries.PeriodFocusMetrics::startOn,
                        FocusMetricsSeries.PeriodFocusMetrics::endOn
                )
                .containsExactly(
                        tuple(startOn, LocalDate.of(2026, 9, 30)),
                        tuple(LocalDate.of(2026, 10, 1), endOn)
                );
    }
}
