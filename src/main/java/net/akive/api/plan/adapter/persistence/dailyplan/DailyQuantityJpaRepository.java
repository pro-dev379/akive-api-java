package net.akive.api.plan.adapter.persistence.dailyplan;

import net.akive.api.plan.adapter.persistence.entity.DailyQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DailyQuantityJpaRepository extends JpaRepository<DailyQuantityEntity, Long> {

    @Query("""
        select dq
        from DailyQuantityEntity dq
        join fetch dq.weeklyQuantity wq
        join fetch wq.totalQuantity tq
        join fetch tq.sessionalPlan sp
        where sp.user.userId = :userId
            and dq.date = :date
        order by sp.sessionalPlanId asc, tq.totalQuantityId asc, wq.weeklyQuantityId asc
    """)
    List<DailyQuantityEntity> findAllDailyQuantityByUserIdAndDateOrderByPlanHierarchyAsc(Long userId, LocalDate date);
}
