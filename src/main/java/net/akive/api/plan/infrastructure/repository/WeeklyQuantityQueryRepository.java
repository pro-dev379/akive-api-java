package net.akive.api.plan.infrastructure.repository;

import net.akive.api.plan.infrastructure.entity.WeekPeriodEntity;
import net.akive.api.plan.infrastructure.projection.WeeklyPlanWeeklyQuantityProjection;

import java.util.List;

public interface WeeklyQuantityQueryRepository {

    List<WeeklyPlanWeeklyQuantityProjection> findWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionPlanId,
            WeekPeriodEntity weekPeriod
    );
}