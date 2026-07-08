package net.akive.api.plan.infrastructure;

import net.akive.api.plan.infrastructure.projection.DailyTodoProjection;

import java.time.LocalDate;
import java.util.List;

public interface SessionalPlanQueryRepository {

    List<DailyTodoProjection> findDailyTodosByUserIdAndDate(Long userId, LocalDate date);
}
