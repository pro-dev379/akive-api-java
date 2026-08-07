package net.akive.api.plan.application.required.dto;

import net.akive.api.plan.domain.SessionWeek;

public record SessionalPlanWeeklyQuantityData(
        Long weeklyQuantityId,
        Long totalQuantityId,
        SessionWeek sessionWeek,
        SessionalPlanWeeklyQuantityAmountData weeklyQuantityAmount
) {
}
