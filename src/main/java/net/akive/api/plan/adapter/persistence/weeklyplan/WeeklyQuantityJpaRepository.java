package net.akive.api.plan.adapter.persistence.weeklyplan;

import net.akive.api.plan.adapter.persistence.entity.WeekPeriodEntity;
import net.akive.api.plan.adapter.persistence.entity.WeeklyQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WeeklyQuantityJpaRepository extends JpaRepository<WeeklyQuantityEntity, Long> {

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
