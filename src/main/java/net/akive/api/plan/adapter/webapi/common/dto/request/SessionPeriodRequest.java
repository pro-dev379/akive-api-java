package net.akive.api.plan.adapter.webapi.common.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SessionPeriodRequest(@NotNull LocalDate startOn, @NotNull LocalDate endOn) {
}
