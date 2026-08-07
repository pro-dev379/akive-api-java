package net.akive.api.plan.application.provided.dto.result.common;

import java.time.LocalDate;

public record WeekPeriodResult(
        LocalDate startOn,
        LocalDate endOn
) {
}
