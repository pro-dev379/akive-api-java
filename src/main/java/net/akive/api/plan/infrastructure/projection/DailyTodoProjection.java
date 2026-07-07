package net.akive.api.plan.infrastructure.projection;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record DailyTodoProjection(
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

    @QueryProjection
    public DailyTodoProjection {}
}
