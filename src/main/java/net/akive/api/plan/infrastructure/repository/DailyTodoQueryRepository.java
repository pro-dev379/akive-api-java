package net.akive.api.plan.infrastructure.repository;

import net.akive.api.plan.infrastructure.projection.DailyTodoProjection;

import java.time.LocalDate;
import java.util.List;

public interface DailyTodoQueryRepository {

    List<DailyTodoProjection> findDailyTodosByUserIdAndDate(Long userId, LocalDate date);
}