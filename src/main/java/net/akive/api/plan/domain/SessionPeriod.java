package net.akive.api.plan.domain;

import net.akive.api.common.exception.business.BadRequestException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.exception.business.BadRequestError.INVALID_SESSION_PERIOD;
import static net.akive.api.common.exception.business.BadRequestError.SESSION_PERIOD_MAX_MONTHS_EXCEEDED;
import static net.akive.api.plan.domain.PlanPolicy.DAYS_PER_WEEK;
import static net.akive.api.plan.domain.PlanPolicy.SESSION_PERIOD_MAX_MONTHS;

public record SessionPeriod(LocalDate startOn, LocalDate endOn) {

    public SessionPeriod {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (!startOn.isBefore(endOn)) {
            throw new BadRequestException(INVALID_SESSION_PERIOD);
        }

        if (endOn.isAfter(startOn.plusMonths(SESSION_PERIOD_MAX_MONTHS))) {
            throw new BadRequestException(SESSION_PERIOD_MAX_MONTHS_EXCEEDED);
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

            sessionWeeks.add(SessionWeek.of(weekNum++, weekStartOn, weekEndOn));
            weekStartOn = weekEndOn.plusDays(1);
        }

        return List.copyOf(sessionWeeks);
    }

    public Optional<SessionWeek> findSessionWeek(int weekNum) {
        if (weekNum < 1) {
            return Optional.empty();
        }

        LocalDate firstWeekEndOn = startOn.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        if (weekNum == 1) {
            return Optional.of(SessionWeek.of(weekNum, startOn, min(firstWeekEndOn, endOn)));
        }

        LocalDate weekStartOn = firstWeekEndOn.plusDays(1).plusWeeks(weekNum - 2L);
        if (weekStartOn.isAfter(endOn)) {
            return Optional.empty();
        }

        return Optional.of(SessionWeek.of(weekNum, weekStartOn, min(weekStartOn.plusDays(DAYS_PER_WEEK - 1), endOn)));
    }

    private static LocalDate min(LocalDate first, LocalDate second) {
        if (first.isAfter(second)) {
            return second;
        }

        return first;
    }
}
