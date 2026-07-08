package net.akive.api.plan.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.akive.api.plan.infrastructure.entity.QDailyQuantityEntity;
import net.akive.api.plan.infrastructure.entity.QSessionalPlanEntity;
import net.akive.api.plan.infrastructure.entity.QTotalQuantityEntity;
import net.akive.api.plan.infrastructure.entity.QWeeklyQuantityEntity;
import net.akive.api.plan.infrastructure.projection.DailyTodoProjection;
import net.akive.api.plan.infrastructure.projection.QDailyTodoProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SessionalPlanQueryRepositoryImpl implements SessionalPlanQueryRepository {

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
                        tq.subject,
                        tq.subjectCategory,
                        dq.dailyQuantityAmount,
                        dq.completedQuantityAmount,
                        dq.delayQuantityAmount,
                        tq.unit,
                        dq.dailyQuantityMemo,
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
}
