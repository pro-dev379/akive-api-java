package net.akive.api.plan.application.dto.result;

import java.time.LocalDate;

public record DailyTodoResult(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Long dailyQuantityId,
        String subject,
        String subjectCategory,
        int dailyQuantityAmount,
        int completedQuantityAmount,
        int delayQuantityAmount,
        String unit,
        String dailyQuantityMemo,
        LocalDate date
) {
}
