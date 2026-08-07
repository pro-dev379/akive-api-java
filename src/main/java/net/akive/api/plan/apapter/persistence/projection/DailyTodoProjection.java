package net.akive.api.plan.apapter.persistence.projection;

import com.querydsl.core.annotations.QueryProjection;
import net.akive.api.plan.apapter.persistence.entity.DailyQuantityAmountEntity;
import net.akive.api.plan.apapter.persistence.entity.SubjectEntity;

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
