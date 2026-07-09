package net.akive.api.plan.domain;

import java.time.LocalDate;

import net.akive.api.plan.domain.exception.SessionalPlanException;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.plan.domain.exception.SessionalPlanErrorCode.INVALID_SESSION_PERIOD;

public record SessionPeriod(LocalDate startOn, LocalDate endOn) {

    public SessionPeriod {
        requireNonNull(startOn);
        requireNonNull(endOn);

        if (!startOn.isBefore(endOn)) {
            throw new SessionalPlanException(INVALID_SESSION_PERIOD);
        }
    }

    public static SessionPeriod of(LocalDate startOn, LocalDate endOn) {
        return new SessionPeriod(startOn, endOn);
    }
}
