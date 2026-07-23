package net.akive.api.plan.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.akive.api.plan.infrastructure.entity.QDailyQuantityEntity;
import net.akive.api.plan.infrastructure.entity.QTotalQuantityEntity;
import net.akive.api.plan.infrastructure.entity.QWeeklyQuantityEntity;
import net.akive.api.plan.infrastructure.projection.QSessionalPlanTotalQuantityProjection;
import net.akive.api.plan.infrastructure.projection.SessionalPlanTotalQuantityProjection;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.akive.api.plan.infrastructure.entity.QDailyQuantityEntity.dailyQuantityEntity;
import static net.akive.api.plan.infrastructure.entity.QTotalQuantityEntity.totalQuantityEntity;
import static net.akive.api.plan.infrastructure.entity.QWeeklyQuantityEntity.weeklyQuantityEntity;

@Repository
@RequiredArgsConstructor
class SessionalPlanQueryRepositoryImpl implements SessionalPlanQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<SessionalPlanTotalQuantityProjection> findSessionalPlanTotalQuantityDetailsBySessionalPlanId(Long sessionalPlanId) {
        QDailyQuantityEntity dq = dailyQuantityEntity;
        QWeeklyQuantityEntity wq = weeklyQuantityEntity;
        QTotalQuantityEntity tq = totalQuantityEntity;

        return jpaQueryFactory
                .select(
                        new QSessionalPlanTotalQuantityProjection(
                                tq.totalQuantityId,
                                tq.sessionalPlan.sessionalPlanId,
                                tq.subject,
                                tq.totalQuantityAmount,
                                wq.weeklyQuantityId,
                                wq.weekPeriod,
                                wq.weeklyQuantityAmount,
                                dq.dailyQuantityAmount.completedQuantityAmount.sumLong(),
                                dq.dailyQuantityAmount.delayedQuantityAmount.sumLong()
                        )
                )
                .from(tq)
                .join(wq).on(wq.totalQuantity.eq(tq))
                .join(dq).on(dq.weeklyQuantity.eq(wq))
                .where(tq.sessionalPlan.sessionalPlanId.eq(sessionalPlanId))
                .groupBy(
                        tq.totalQuantityId,
                        tq.sessionalPlan.sessionalPlanId,
                        tq.subject,
                        tq.totalQuantityAmount,
                        wq.weeklyQuantityId,
                        wq.weekPeriod,
                        wq.weeklyQuantityAmount
                )
                .orderBy(
                        tq.totalQuantityId.asc(),
                        wq.weekPeriod.startOn.asc()
                )
                .fetch();
    }
}
