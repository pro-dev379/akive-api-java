package net.akive.api.plan.presentation.dto.response;

import net.akive.api.plan.application.dto.result.DailyTodoResult;

import java.time.LocalDate;

public record DailyTodoItemResponse(
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

    public static DailyTodoItemResponse from(DailyTodoResult result) {
        return new DailyTodoItemResponse(
                result.sessionalPlanId(),
                result.totalQuantityId(),
                result.weeklyQuantityId(),
                result.dailyQuantityId(),
                result.subject(),
                result.subjectCategory(),
                result.dailyQuantityAmount(),
                result.completedQuantityAmount(),
                result.delayQuantityAmount(),
                result.unit(),
                result.dailyQuantityMemo(),
                result.date()
        );
    }
}
