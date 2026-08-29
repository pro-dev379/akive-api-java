package net.akive.api.plan.application.required;

import net.akive.api.plan.application.required.data.WeeklyPlanData;
import net.akive.api.plan.domain.value.WeekPeriod;

import java.util.List;
import java.util.Optional;

public interface WeeklyQuantityRepository {

    List<WeeklyPlanData> findAllWeeklyPlanDataBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriod weekPeriod
    );

    Optional<WeeklyPlanData> findWeeklyPlanDataBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
            Long sessionalPlanId,
            Long userId,
            Long totalQuantityId,
            Long weeklyQuantityId
    );
}
