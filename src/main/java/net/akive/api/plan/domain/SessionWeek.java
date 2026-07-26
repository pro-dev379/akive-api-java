package net.akive.api.plan.domain;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.*;

public record SessionWeek(
        int weekNum,
        WeekPeriod weekPeriod
) {

    public SessionWeek {
        requirePositive(weekNum);
        requireNonNull(weekPeriod);
    }

    public static SessionWeek of(int weekNum, LocalDate weekStartOn, LocalDate weekEndOn) {
        return new SessionWeek(weekNum, new WeekPeriod(weekStartOn, weekEndOn));
    }
}
