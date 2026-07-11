package net.akive.api.plan.presentation.dto.response;

import java.time.LocalDate;

public record SessionPeriodResponse(LocalDate startOn, LocalDate endOn) {
}
