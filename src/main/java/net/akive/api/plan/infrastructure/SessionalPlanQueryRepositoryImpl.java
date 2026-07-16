package net.akive.api.plan.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.akive.api.plan.infrastructure.entity.QDailyQuantityEntity;
import net.akive.api.plan.infrastructure.entity.QSessionalPlanEntity;
import net.akive.api.plan.infrastructure.entity.QTotalQuantityEntity;
import net.akive.api.plan.infrastructure.entity.QWeeklyQuantityEntity;
import net.akive.api.plan.infrastructure.projection.DailyTodoProjection;
import net.akive.api.plan.infrastructure.projection.QDailyTodoProjection;
import net.akive.api.plan.infrastructure.projection.QSessionalPlanTotalQuantityProjection;
import net.akive.api.plan.infrastructure.projection.SessionalPlanTotalQuantityProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
class SessionalPlanQueryRepositoryImpl implements SessionalPlanQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<DailyTodoProjection> findDailyTodosByUserIdAndDate(Long userId, LocalDate date) {
        QDailyQuantityEntity dq = QDailyQuantityEntity.dailyQuantityEntity;
        QWeeklyQuantityEntity wq = QWeeklyQuantityEntity.weeklyQuantityEntity;
        QTotalQuantityEntity tq = QTotalQuantityEntity.totalQuantityEntity;
        QSessionalPlanEntity sp = QSessionalPlanEntity.sessionalPlanEntity;

        return jpaQueryFactory
                .select(
                    new QDailyTodoProjection(
                        sp.sessionalPlanId,
                        tq.totalQuantityId,
                        wq.weeklyQuantityId,
                        dq.dailyQuantityId,
                        tq.subject.name,
                        tq.subject.category,
                        dq.dailyQuantityAmount.quantityAmount,
                        dq.dailyQuantityAmount.completedQuantityAmount,
                        dq.dailyQuantityAmount.delayedQuantityAmount,
                        tq.totalQuantityAmount.unit,
                        dq.dailyQuantityAmount.quantityMemo,
                        dq.date
                    )
                )
                .from(dq)
                .join(dq.weeklyQuantity, wq)
                .join(wq.totalQuantity, tq)
                .join(tq.sessionalPlan, sp)
                .where(
                        sp.user.userId.eq(userId),
                        dq.date.eq(date)
                )
                .fetch();
    }

    @Override
    public List<SessionalPlanTotalQuantityProjection> findSessionalPlanTotalQuantityDetailsBySessionalPlanId(Long sessionalPlanId) {
        QDailyQuantityEntity dq = QDailyQuantityEntity.dailyQuantityEntity;
        QWeeklyQuantityEntity wq = QWeeklyQuantityEntity.weeklyQuantityEntity;
        QTotalQuantityEntity tq = QTotalQuantityEntity.totalQuantityEntity;

        return jpaQueryFactory
                .select(
                        new QSessionalPlanTotalQuantityProjection(
                                tq.totalQuantityId,
                                tq.sessionalPlan.sessionalPlanId,
                                tq.subject.name,
                                tq.subject.category,
                                tq.totalQuantityAmount.quantityAmount,
                                tq.totalQuantityAmount.unit,
                                tq.totalQuantityAmount.memo,
                                wq.weeklyQuantityId,
                                wq.weekPeriod.startOn,
                                wq.weekPeriod.endOn,
                                wq.weeklyQuantityAmount.quantityAmount,
                                wq.weeklyQuantityAmount.quantityMemo,
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
                        tq.subject.name,
                        tq.subject.category,
                        tq.totalQuantityAmount.quantityAmount,
                        tq.totalQuantityAmount.unit,
                        tq.totalQuantityAmount.memo,
                        wq.weeklyQuantityId,
                        wq.weekPeriod.startOn,
                        wq.weekPeriod.endOn,
                        wq.weeklyQuantityAmount.quantityAmount,
                        wq.weeklyQuantityAmount.quantityMemo
                )
                .orderBy(
                        tq.totalQuantityId.asc(),
                        wq.weekPeriod.startOn.asc()
                )
                .fetch();
    }
}
