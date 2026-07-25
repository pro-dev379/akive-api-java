package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_WEEK_PERIOD;
import static net.akive.api.plan.domain.PlanPolicy.DAYS_PER_WEEK;

public record WeekPeriod(LocalDate startOn, LocalDate endOn) {

    public WeekPeriod {
        requireStartNotAfterEnd(startOn, endOn);

        long periodDays = ChronoUnit.DAYS.between(startOn, endOn);
        if (periodDays < 0 || DAYS_PER_WEEK < periodDays) {
            throw new BadRequestException(INVALID_WEEK_PERIOD);
        }
    }

    public List<PlanDate> splitIntoDates() {
        return Stream.iterate(startOn, date -> !date.isAfter(endOn), date -> date.plusDays(1))
                .map(PlanDate::from)
                .toList();
    }
}
