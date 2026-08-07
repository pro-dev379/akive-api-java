package net.akive.api.plan.apapter.persistence.projection;

import com.querydsl.core.annotations.QueryProjection;
import net.akive.api.plan.apapter.persistence.entity.DailyQuantityAmountEntity;
import net.akive.api.plan.apapter.persistence.entity.SubjectEntity;
import net.akive.api.plan.apapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.apapter.persistence.entity.WeeklyQuantityAmountEntity;

import java.time.LocalDate;

public record WeeklyPlanWeeklyQuantityProjection(
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
    public WeeklyPlanWeeklyQuantityProjection {
    }
}
