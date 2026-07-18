package net.akive.api.plan.domain;

import net.akive.api.plan.exception.PlanException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

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

    public List<PlanDate> splitIntoDates() {
        return Stream.iterate(startOn, date -> !date.isAfter(endOn), date -> date.plusDays(1))
                .map(PlanDate::from)
                .toList();
    }
}
