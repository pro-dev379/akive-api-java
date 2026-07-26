package net.akive.api.plan.infrastructure.projection;

import com.querydsl.core.annotations.QueryProjection;
import net.akive.api.plan.infrastructure.entity.SubjectEntity;
import net.akive.api.plan.infrastructure.entity.TotalQuantityAmountEntity;
import net.akive.api.plan.infrastructure.entity.WeekPeriodEntity;
import net.akive.api.plan.infrastructure.entity.WeeklyQuantityAmountEntity;

public record SessionalPlanTotalQuantityProjection(
        Long totalQuantityId,
        Long sessionalPlanId,
        SubjectEntity subject,
        TotalQuantityAmountEntity totalQuantityAmount,
        Long weeklyQuantityId,
        WeekPeriodEntity weekPeriod,
        WeeklyQuantityAmountEntity weeklyQuantityAmount,
        long weeklyCompletedQuantityAmount,
        long weeklyDelayAmount
) {

    @QueryProjection
    public SessionalPlanTotalQuantityProjection {
    }
}
