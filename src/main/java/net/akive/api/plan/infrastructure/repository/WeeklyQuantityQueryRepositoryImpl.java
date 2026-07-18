package net.akive.api.plan.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.akive.api.plan.infrastructure.entity.*;
import net.akive.api.plan.infrastructure.projection.QWeeklyPlanWeeklyQuantityProjection;
import net.akive.api.plan.infrastructure.projection.WeeklyPlanWeeklyQuantityProjection;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WeeklyQuantityQueryRepositoryImpl implements WeeklyQuantityQueryRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WeeklyPlanWeeklyQuantityProjection> findWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionPlanId,
            WeekPeriodEntity weekPeriod
    ) {
        QTotalQuantityEntity tq = QTotalQuantityEntity.totalQuantityEntity;
        QWeeklyQuantityEntity wq = QWeeklyQuantityEntity.weeklyQuantityEntity;
        QDailyQuantityEntity dq = QDailyQuantityEntity.dailyQuantityEntity;

        return jpaQueryFactory
                .select(
                        new QWeeklyPlanWeeklyQuantityProjection(
                                tq.sessionalPlan.sessionalPlanId,
                                tq.totalQuantityId,
                                wq.weeklyQuantityId,
                                dq.dailyQuantityId,
                                tq.subject,
                                wq.weeklyQuantityAmount,
                                tq.totalQuantityAmount.unit,
                                wq.weekPeriod,
                                dq.dailyQuantityAmount,
                                dq.date
                        )
                )
                .from(tq)
                .join(wq).on(wq.totalQuantity.eq(tq))
                .join(dq).on(dq.weeklyQuantity.eq(wq))
                .where(
                        tq.sessionalPlan.sessionalPlanId.eq(sessionPlanId),
                        wq.weekPeriod.eq(weekPeriod)
                )
                .fetch();
    }
}