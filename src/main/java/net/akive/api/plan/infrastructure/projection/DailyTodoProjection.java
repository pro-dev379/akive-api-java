package net.akive.api.plan.infrastructure.projection;

import com.querydsl.core.annotations.QueryProjection;
import net.akive.api.plan.infrastructure.entity.DailyQuantityAmountEntity;
import net.akive.api.plan.infrastructure.entity.SubjectEntity;

import java.time.LocalDate;

public record DailyTodoProjection(
        Long sessionalPlanId,
        Long totalQuantityId,
        Long weeklyQuantityId,
        Long dailyQuantityId,
        SubjectEntity subject,
        DailyQuantityAmountEntity dailyQuantityAmount,
        String unit,
        LocalDate date
) {

    @QueryProjection
    public DailyTodoProjection {}
}
