package net.akive.api.plan.infrastructure.projection;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDate;

public record SessionalPlanTotalQuantityProjection(
        Long totalQuantityId,
        Long sessionalPlanId,
        String subject,
        String subjectCategory,
        int totalQuantityAmount,
        String unit,
        String totalQuantityMemo,
        Long weeklyQuantityId,
        LocalDate weekStartOn,
        LocalDate weekEndOn,
        int weeklyQuantityAmount,
        String weeklyQuantityMemo,
        long weeklyCompletedQuantityAmount,
        long weeklyDelayAmount
) {

    @QueryProjection
    public SessionalPlanTotalQuantityProjection {
    }
}