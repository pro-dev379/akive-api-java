package net.akive.api.plan.application.required.dto;

import net.akive.api.plan.domain.DailyQuantityAmount;
import net.akive.api.plan.domain.PlanDate;

public record WeeklyPlanDailyQuantityData(
        Long dailyQuantityId,
        DailyQuantityAmount dailyQuantityAmount,
        PlanDate planDate
) {
}
