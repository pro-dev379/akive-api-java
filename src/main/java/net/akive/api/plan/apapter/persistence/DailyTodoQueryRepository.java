package net.akive.api.plan.apapter.persistence;

import net.akive.api.plan.apapter.persistence.projection.DailyTodoProjection;

import java.time.LocalDate;
import java.util.List;

public interface DailyTodoQueryRepository {

    List<DailyTodoProjection> findDailyTodosByUserIdAndDate(Long userId, LocalDate date);
}
