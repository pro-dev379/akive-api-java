package net.akive.api.plan.presentation.dto.response;

import java.time.LocalDate;

public record SessionPeriodResponse(LocalDate startOn, LocalDate endOn) {

    public static SessionPeriodResponse of(LocalDate startOn, LocalDate endOn) {
        return new SessionPeriodResponse(startOn, endOn);
    }
}
