package net.akive.api.plan.apapter.persistence.projection;

import com.querydsl.core.annotations.QueryProjection;
import net.akive.api.plan.apapter.persistence.entity.SubjectEntity;
import net.akive.api.plan.apapter.persistence.entity.TotalQuantityAmountEntity;
import net.akive.api.plan.apapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.apapter.persistence.entity.WeeklyQuantityAmountEntity;

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
