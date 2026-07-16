package net.akive.api.plan.infrastructure.projection;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record DailyTodoProjection(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Long dailyQuantityId,
        String subjectName,
        String subjectCategory,
        int dailyQuantityAmount,
        int completedQuantityAmount,
        int delayedQuantityAmount,
        String unit,
        String dailyQuantityMemo,
        LocalDate date
) {

    @QueryProjection
    public DailyTodoProjection {}
}
