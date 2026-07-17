package net.akive.api.plan.application.dto.result.common;

import net.akive.api.plan.domain.SessionPeriod;

import java.time.LocalDate;

public record SessionPeriodResult(
        LocalDate startOn,
        LocalDate endOn
) {

    public static SessionPeriodResult from(SessionPeriod sessionPeriod) {
        return new SessionPeriodResult(sessionPeriod.startOn(), sessionPeriod.endOn());
    }
}