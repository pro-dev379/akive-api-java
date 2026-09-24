package net.akive.api.dailylog.domain;

import net.akive.api.user.domain.GoalVersion;
import net.akive.api.common.analytics.TimeSeriesUnit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;

public record FocusMetricsSeries(List<PeriodFocusMetrics> periodFocusMetrics) {

    public FocusMetricsSeries {
        periodFocusMetrics = List.copyOf(periodFocusMetrics);
    }

    public static FocusMetricsSeries calculate(
            LocalDate startOn,
            LocalDate endOn,
            TimeSeriesUnit unit,
            List<DailyLog> dailyLogs,
            List<GoalVersion> goals
    ) {
        requireStartNotAfterEnd(startOn, endOn);
        requireNonNull(unit);

        List<PeriodFocusMetrics> periodFocusMetrics = new ArrayList<>();
        LocalDate periodStartOn = startOn;

        while (!periodStartOn.isAfter(endOn)) {
            LocalDate periodEndOn = min(unit.periodEndOn(periodStartOn), endOn);
            FocusMetrics focusMetrics = FocusMetrics.calculate(periodStartOn, periodEndOn, dailyLogs, goals);

            periodFocusMetrics.add(new PeriodFocusMetrics(periodStartOn, periodEndOn, focusMetrics));
            periodStartOn = periodEndOn.plusDays(1);
        }

        return new FocusMetricsSeries(periodFocusMetrics);
    }

    private static LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    public record PeriodFocusMetrics(LocalDate startOn, LocalDate endOn, FocusMetrics focusMetrics) {
    }
}
