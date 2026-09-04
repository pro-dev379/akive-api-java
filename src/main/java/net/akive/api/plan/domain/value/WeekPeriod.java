package net.akive.api.plan.domain.value;

import net.akive.api.common.exception.business.BusinessRuleViolationException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static net.akive.api.common.domain.DomainValidator.requireStartNotAfterEnd;
import static net.akive.api.common.exception.business.BusinessRuleViolationError.INVALID_WEEK_PERIOD;
import static net.akive.api.plan.domain.policy.PlanPolicy.DAYS_PER_WEEK;

public record WeekPeriod(LocalDate startOn, LocalDate endOn) {

    public WeekPeriod {
        requireStartNotAfterEnd(startOn, endOn);

        long inclusiveDays = ChronoUnit.DAYS.between(startOn, endOn) + 1;
        if (inclusiveDays > DAYS_PER_WEEK) {
            throw new BusinessRuleViolationException(INVALID_WEEK_PERIOD);
        }
    }

    public List<PlanDate> splitIntoDates() {
        return Stream.iterate(
                        startOn,
                        date -> !date.isAfter(endOn),
                        date -> date.plusDays(1)
                )
                .map(PlanDate::from)
                .toList();
    }
}
