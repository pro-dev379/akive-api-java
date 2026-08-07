package net.akive.api.plan.apapter.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.akive.api.plan.apapter.persistence.entity.QDailyQuantityEntity;
import net.akive.api.plan.apapter.persistence.entity.QSessionalPlanEntity;
import net.akive.api.plan.apapter.persistence.entity.QTotalQuantityEntity;
import net.akive.api.plan.apapter.persistence.entity.QWeeklyQuantityEntity;
import net.akive.api.plan.apapter.persistence.projection.DailyTodoProjection;
import net.akive.api.plan.apapter.persistence.projection.QDailyTodoProjection;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DailyTodoQueryRepositoryImpl implements DailyTodoQueryRepository {

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
                                dq.dailyQuantityAmount,
                                tq.totalQuantityAmount.unit,
                                dq.date
                        )
                )
                .from(sp)
                .join(tq).on(tq.sessionalPlan.eq(sp))
                .join(wq).on(wq.totalQuantity.eq(tq))
                .join(dq).on(dq.weeklyQuantity.eq(wq))
                .where(
                        sp.user.userId.eq(userId),
                        dq.date.eq(date)
                )
                .fetch();
    }
}
