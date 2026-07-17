package net.akive.api.plan.domain;

import net.akive.api.plan.exception.PlanException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;
import static net.akive.api.plan.exception.PlanErrorCode.INVALID_WEEK_PERIOD;

public record WeekPeriod(LocalDate startOn, LocalDate endOn) {

    private static final long WEEK_DAYS = 7L;

    public WeekPeriod {
        requireStartNotAfterEnd(startOn, endOn);

        long periodDays = ChronoUnit.DAYS.between(startOn, endOn);
        if (periodDays < 0 || WEEK_DAYS < periodDays) {
            throw new PlanException(INVALID_WEEK_PERIOD);
        }
    }
}
