package net.akive.api.plan.application.required;

import net.akive.api.plan.application.required.dto.DailyTodoData;

import java.time.LocalDate;
import java.util.List;

public interface LoadDailyTodosPort {

    List<DailyTodoData> loadDailyTodosByUserIdAndDate(Long userId, LocalDate date);
}
