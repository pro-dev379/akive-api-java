package net.akive.api.plan.apapter.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.akive.api.plan.apapter.persistence.entity.QDailyQuantityEntity;
import net.akive.api.plan.apapter.persistence.entity.QTotalQuantityEntity;
import net.akive.api.plan.apapter.persistence.entity.QWeeklyQuantityEntity;
import net.akive.api.plan.apapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.apapter.persistence.projection.QWeeklyPlanWeeklyQuantityProjection;
import net.akive.api.plan.apapter.persistence.projection.WeeklyPlanWeeklyQuantityProjection;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.akive.api.plan.apapter.persistence.entity.QDailyQuantityEntity.dailyQuantityEntity;
import static net.akive.api.plan.apapter.persistence.entity.QTotalQuantityEntity.totalQuantityEntity;
import static net.akive.api.plan.apapter.persistence.entity.QWeeklyQuantityEntity.weeklyQuantityEntity;

@Repository
@RequiredArgsConstructor
public class WeeklyQuantityQueryRepositoryImpl implements WeeklyQuantityQueryRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WeeklyPlanWeeklyQuantityProjection> findWeeklyPlanWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionPlanId,
            WeekPeriodEntity weekPeriod
    ) {
        QTotalQuantityEntity tq = totalQuantityEntity;
        QWeeklyQuantityEntity wq = weeklyQuantityEntity;
        QDailyQuantityEntity dq = dailyQuantityEntity;

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
