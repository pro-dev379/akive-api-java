package net.akive.api.plan.application.port.out.dto;

import java.util.List;

public record SessionalPlanTotalQuantityData(
        Long totalQuantityId,
        Long sessionalPlanId,
        String subject,
        String subjectCategory,
        int totalQuantityAmount,
        String unit,
        String totalQuantityMemo,
        List<SessionalPlanWeeklyQuantityData> weeklyQuantities
) {
}