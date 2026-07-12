package net.akive.api.plan.domain;

import java.time.LocalDate;

import net.akive.api.plan.domain.exception.SessionalPlanException;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.INVALID_SESSION_PERIOD;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.SESSION_PERIOD_TOO_LONG;

public record SessionPeriod(LocalDate startOn, LocalDate endOn) {

    private static final int MAX_DURATION_MONTHS = 12;

    public SessionPeriod {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (!startOn.isBefore(endOn)) {
            throw new SessionalPlanException(INVALID_SESSION_PERIOD);
        }

        if (endOn.isAfter(startOn.plusMonths(MAX_DURATION_MONTHS))) {
            throw new SessionalPlanException(SESSION_PERIOD_TOO_LONG);
        }
    }
}
