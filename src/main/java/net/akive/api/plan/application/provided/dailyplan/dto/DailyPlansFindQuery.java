package net.akive.api.plan.application.provided.dailyplan.dto;

import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;
import static net.akive.api.common.domain.DomainValidator.requirePositive;

public record DailyPlansFindQuery(Long userId, LocalDate date) {

    public DailyPlansFindQuery {
        requirePositive(userId);
        requireNonNull(date);
    }
}
