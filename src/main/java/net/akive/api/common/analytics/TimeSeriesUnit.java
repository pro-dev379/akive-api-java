package net.akive.api.common.analytics;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.SUNDAY;

public enum TimeSeriesUnit {

    DAY {
        @Override
        public LocalDate periodEndOn(LocalDate periodStartOn) {
            return periodStartOn;
        }
    },
    WEEK {
        @Override
        public LocalDate periodEndOn(LocalDate periodStartOn) {
            return periodStartOn.with(TemporalAdjusters.nextOrSame(SUNDAY));
        }
    },
    MONTH {
        @Override
        public LocalDate periodEndOn(LocalDate periodStartOn) {
            return periodStartOn.with(TemporalAdjusters.lastDayOfMonth());
        }
    };

    public abstract LocalDate periodEndOn(LocalDate periodStartOn);
}
