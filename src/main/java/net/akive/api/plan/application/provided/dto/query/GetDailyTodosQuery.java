package net.akive.api.plan.application.provided.dto.query;

import java.time.LocalDate;

public record GetDailyTodosQuery(Long userId, LocalDate date) {
}
