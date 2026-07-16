package net.akive.api.plan.application.port.out.dto;

import java.time.LocalDate;

public record SessionalPlanWeeklyQuantityData(
        Long weeklyQuantityId,
        Long totalQuantityId,
        int weekNum,
        LocalDate weekStartOn,
        LocalDate weekEndOn,
        SessionalPlanWeeklyQuantityAmountData weeklyQuantityAmount
) {
}