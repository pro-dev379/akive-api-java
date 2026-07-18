package net.akive.api.plan.application.port.out.dto;

import net.akive.api.plan.domain.Subject;
import net.akive.api.plan.domain.TotalQuantityAmount;

import java.util.List;

public record SessionalPlanTotalQuantityData(
        Long totalQuantityId,
        Long sessionalPlanId,
        Subject subject,
        TotalQuantityAmount totalQuantityAmount,
        List<SessionalPlanWeeklyQuantityData> weeklyQuantities
) {
}