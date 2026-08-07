package net.akive.api.plan.application.required;

import net.akive.api.plan.application.required.dto.WeeklyPlanWeeklyQuantityData;
import net.akive.api.plan.domain.WeekPeriod;

import java.util.List;

public interface LoadWeeklyQuantityPort {

    List<WeeklyPlanWeeklyQuantityData> loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriod weekPeriod
    );
}
