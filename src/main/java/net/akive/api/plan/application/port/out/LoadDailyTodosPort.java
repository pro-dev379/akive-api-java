package net.akive.api.plan.application.port.out;

import net.akive.api.plan.application.dto.result.DailyTodoResult;

import java.time.LocalDate;
import java.util.List;

public interface LoadDailyTodosPort {

    List<DailyTodoResult> loadDailyTodos(Long userId, LocalDate date);
}
