package net.akive.api.plan.application.dto.command;

import java.time.LocalDate;

public record GetDailyTodosCommand(Long userId, LocalDate date) {

    public static GetDailyTodosCommand of(Long userId, LocalDate date) {
        return new GetDailyTodosCommand(userId, date);
    }
}
