package net.akive.api.plan.infrastructure;

import net.akive.api.plan.infrastructure.projection.DailyTodoProjection;
import net.akive.api.plan.infrastructure.projection.SessionalPlanTotalQuantityProjection;

import java.time.LocalDate;
import java.util.List;

public interface SessionalPlanQueryRepository {

    List<DailyTodoProjection> findDailyTodosByUserIdAndDate(Long userId, LocalDate date);

    List<SessionalPlanTotalQuantityProjection> findSessionalPlanTotalQuantityDetailsBySessionalPlanId(
            Long sessionalPlanId
    );
}
