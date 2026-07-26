package net.akive.api.plan.application.port.out;

import net.akive.api.plan.application.port.out.dto.WeeklyPlanWeeklyQuantityData;
import net.akive.api.plan.domain.WeekPeriod;

import java.util.List;

public interface LoadWeeklyQuantityPort {

    List<WeeklyPlanWeeklyQuantityData> loadWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriod weekPeriod
    );
}
