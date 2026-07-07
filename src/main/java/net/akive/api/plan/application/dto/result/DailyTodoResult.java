package net.akive.api.plan.application.dto.result;

import java.time.LocalDate;

public record DailyTodoResult(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Long dailyQuantityId,
        String subject,
        String subjectCategory,
        Integer dailyQuantityAmount,
        Integer completedQuantityAmount,
        Integer delayQuantityAmount,
        String unit,
        String dailyQuantityMemo,
        LocalDate date
) {
}
