package net.akive.api.plan.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SessionPeriodRequest(
        @NotNull LocalDate startOn,
        @NotNull LocalDate endOn
) {

    public static SessionPeriodRequest of(LocalDate startOn, LocalDate endOn) {
        return new SessionPeriodRequest(startOn, endOn);
    }
}
