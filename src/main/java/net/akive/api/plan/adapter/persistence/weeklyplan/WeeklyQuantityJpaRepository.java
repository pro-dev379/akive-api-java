package net.akive.api.plan.adapter.persistence.weeklyplan;

import net.akive.api.plan.adapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.adapter.persistence.entity.WeeklyQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WeeklyQuantityJpaRepository extends JpaRepository<WeeklyQuantityEntity, Long> {

    @Query("""
        select distinct wq
        from WeeklyQuantityEntity wq
        join fetch wq.totalQuantity tq
        join fetch tq.sessionalPlan sp
        join fetch wq.dailyQuantities dq
        where sp.sessionalPlanId = :sessionalPlanId
            and sp.user.userId = :userId
            and wq.weeklyQuantityId = :weeklyQuantityId
            and wq.totalQuantity.totalQuantityId = :totalQuantityId
        order by wq.weeklyQuantityId asc, dq.date asc
    """)
    Optional<WeeklyQuantityEntity> findBySessionalPlanIdAndUserIdAndTotalQuantityIdAndWeeklyQuantityId(
            Long sessionalPlanId,
            Long userId,
            Long totalQuantityId,
            Long weeklyQuantityId
    );

    @Query("""
        select distinct wq
        from WeeklyQuantityEntity wq
        join fetch wq.totalQuantity tq
        join fetch tq.sessionalPlan sp
        join fetch wq.dailyQuantities dq
        where sp.sessionalPlanId = :sessionalPlanId
            and wq.weekPeriod = :weekPeriod
        order by tq.totalQuantityId asc, dq.date asc
    """)
    List<WeeklyQuantityEntity> findWeeklyQuantitiesBySessionalPlanIdAndWeekPeriod(
            Long sessionalPlanId,
            WeekPeriodEntity weekPeriod
    );
}
