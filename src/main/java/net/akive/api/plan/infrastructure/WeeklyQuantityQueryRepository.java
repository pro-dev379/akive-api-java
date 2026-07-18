package net.akive.api.plan.infrastructure;

import net.akive.api.plan.infrastructure.entity.WeekPeriodEntity;
import net.akive.api.plan.infrastructure.projection.WeeklyPlanWeeklyQuantitiesProjection;

import java.util.List;

public interface WeeklyQuantityQueryRepository {

    List<WeeklyPlanWeeklyQuantitiesProjection> findWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionPlanId,
            WeekPeriodEntity weekPeriod
    );
}