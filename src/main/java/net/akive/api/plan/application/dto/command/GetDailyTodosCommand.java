package net.akive.api.plan.application.dto.command;

import java.time.LocalDate;

public record GetDailyTodosCommand(Long userId, LocalDate date) {
}
