package net.akive.api.dailylog.domain;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.SUNDAY;

public enum FocusMetricsUnit {

    DAY {
        @Override
        LocalDate periodEndOn(LocalDate periodStartOn) {
            return periodStartOn;
        }
    },
    WEEK {
        @Override
        LocalDate periodEndOn(LocalDate periodStartOn) {
            return periodStartOn.with(TemporalAdjusters.nextOrSame(SUNDAY));
        }
    },
    MONTH {
        @Override
        LocalDate periodEndOn(LocalDate periodStartOn) {
            return periodStartOn.with(TemporalAdjusters.lastDayOfMonth());
        }
    };

    abstract LocalDate periodEndOn(LocalDate periodStartOn);
}
