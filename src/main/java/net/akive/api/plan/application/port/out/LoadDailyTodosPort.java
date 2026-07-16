package net.akive.api.plan.application.port.out;

import net.akive.api.plan.application.port.out.dto.DailyTodoData;

import java.time.LocalDate;
import java.util.List;

public interface LoadDailyTodosPort {

    List<DailyTodoData> loadDailyTodosByUserIdAndDate(Long userId, LocalDate date);
}
