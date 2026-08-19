package net.akive.api.plan.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static net.akive.api.common.domain.DomainValidator.requireNonNull;

public record PlanDate(LocalDate date, DayOfWeek dayOfWeek) {

    public PlanDate {
        requireNonNull(date);
        requireNonNull(dayOfWeek);
    }

    public static PlanDate from(LocalDate date) {
        return new PlanDate(date, date.getDayOfWeek());
    }
}
