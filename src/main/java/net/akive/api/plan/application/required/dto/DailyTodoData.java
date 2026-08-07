package net.akive.api.plan.application.required.dto;

import net.akive.api.plan.domain.DailyQuantityAmount;
import net.akive.api.plan.domain.Subject;

import java.time.LocalDate;

public record DailyTodoData(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Long dailyQuantityId,
        Subject subject,
        DailyQuantityAmount dailyQuantityAmount,
        String unit,
        LocalDate date
) {
}
