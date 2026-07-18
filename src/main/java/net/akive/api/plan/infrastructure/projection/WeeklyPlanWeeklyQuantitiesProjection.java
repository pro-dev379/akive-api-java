package net.akive.api.plan.infrastructure.projection;

import com.querydsl.core.annotations.QueryProjection;
import net.akive.api.plan.infrastructure.entity.DailyQuantityAmountEntity;
import net.akive.api.plan.infrastructure.entity.SubjectEntity;
import net.akive.api.plan.infrastructure.entity.WeekPeriodEntity;
import net.akive.api.plan.infrastructure.entity.WeeklyQuantityAmountEntity;

import java.time.LocalDate;

public record WeeklyPlanWeeklyQuantitiesProjection(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Long DailyQuantityId,
        SubjectEntity subject,
        WeeklyQuantityAmountEntity weeklyQuantityAmount,
        String unit,
        WeekPeriodEntity weekPeriod,
        DailyQuantityAmountEntity dailyQuantityAmount,
        LocalDate date
) {

    @QueryProjection
    public WeeklyPlanWeeklyQuantitiesProjection {
    }
}