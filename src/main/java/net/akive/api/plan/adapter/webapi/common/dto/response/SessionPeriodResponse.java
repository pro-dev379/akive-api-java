package net.akive.api.plan.adapter.webapi.common.dto.response;

import net.akive.api.plan.domain.value.SessionPeriod;

import java.time.LocalDate;

public record SessionPeriodResponse(LocalDate startOn, LocalDate endOn) {

    public static SessionPeriodResponse from(SessionPeriod sessionPeriod) {
        return new SessionPeriodResponse(sessionPeriod.startOn(), sessionPeriod.endOn());
    }
}
