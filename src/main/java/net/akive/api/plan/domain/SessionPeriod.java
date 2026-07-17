package net.akive.api.plan.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import net.akive.api.plan.exception.PlanException;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.plan.exception.PlanErrorCode.INVALID_SESSION_PERIOD;
import static net.akive.api.plan.exception.PlanErrorCode.SESSION_PERIOD_TOO_LONG;

public record SessionPeriod(LocalDate startOn, LocalDate endOn) {

    private static final int MAX_DURATION_MONTHS = 12;

    public SessionPeriod {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (!startOn.isBefore(endOn)) {
            throw new PlanException(INVALID_SESSION_PERIOD);
        }

        if (endOn.isAfter(startOn.plusMonths(MAX_DURATION_MONTHS))) {
            throw new PlanException(SESSION_PERIOD_TOO_LONG);
        }
    }

    public List<SessionWeek> splitIntoWeeks() {
        List<SessionWeek> sessionWeeks = new ArrayList<>();

        LocalDate weekStartOn = this.startOn;
        int weekNum = 1;
        while (!weekStartOn.isAfter(this.endOn)) {
            LocalDate weekEndOn = weekStartOn
                    .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            if (weekEndOn.isAfter(this.endOn)) {
                weekEndOn = this.endOn;
            }

            sessionWeeks.add(new SessionWeek(
                    weekNum++,
                    new WeekPeriod(weekStartOn, weekEndOn)
            ));
            weekStartOn = weekEndOn.plusDays(1);
        }

        return List.copyOf(sessionWeeks);
    }
}
