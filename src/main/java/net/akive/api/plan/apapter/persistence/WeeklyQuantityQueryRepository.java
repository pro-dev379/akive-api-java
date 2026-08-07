package net.akive.api.plan.apapter.persistence;

import net.akive.api.plan.apapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.apapter.persistence.projection.WeeklyPlanWeeklyQuantityProjection;

import java.util.List;

public interface WeeklyQuantityQueryRepository {

    List<WeeklyPlanWeeklyQuantityProjection> findWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionPlanId,
            WeekPeriodEntity weekPeriod
    );
}
