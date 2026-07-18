package net.akive.api.plan.application.port.out.dto;

import net.akive.api.plan.domain.DailyQuantityAmount;
import net.akive.api.plan.domain.PlanDate;

public record WeeklyPlanDailyQuantityData(
        Long dailyQuantityId,
        DailyQuantityAmount dailyQuantityAmount,
        PlanDate planDate
) {
}