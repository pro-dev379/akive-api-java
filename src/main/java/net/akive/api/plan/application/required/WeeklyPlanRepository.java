package net.akive.api.plan.application.required;

import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.domain.value.WeekPeriod;

import java.util.List;

public interface WeeklyPlanRepository {

    List<WeeklyPlanData> findWeeklyPlanDataBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriod weekPeriod
    );
}
