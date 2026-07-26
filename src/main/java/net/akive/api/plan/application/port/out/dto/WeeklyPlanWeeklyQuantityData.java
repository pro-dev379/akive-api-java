package net.akive.api.plan.application.port.out.dto;

import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.WeeklyQuantityAmount;

import java.util.List;

public record WeeklyPlanWeeklyQuantityData(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Subject subject,
        WeeklyQuantityAmount weeklyQuantityAmount,
        String unit,
        List<WeeklyPlanDailyQuantityData> dailyQuantities
) {
}
