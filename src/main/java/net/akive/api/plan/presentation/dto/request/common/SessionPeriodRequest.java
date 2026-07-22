package net.akive.api.plan.presentation.dto.request.common;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SessionPeriodRequest(
        @NotNull LocalDate startOn,
        @NotNull LocalDate endOn
) {
}
