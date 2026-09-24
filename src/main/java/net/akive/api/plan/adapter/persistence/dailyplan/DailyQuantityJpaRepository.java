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
    List<DailyQuantityEntity> findAllByUserIdAndDateOrderByPlanHierarchyAsc(Long userId, LocalDate date);

    @Query("""
        select
            sum(dq.dailyQuantityAmount.quantityAmount) as quantityAmount,
            sum(dq.dailyQuantityAmount.completedQuantityAmount) as completedQuantityAmount
        from DailyQuantityEntity dq
        join dq.weeklyQuantity wq
        join wq.totalQuantity tq
        join tq.sessionalPlan sp
        where sp.user.userId = :userId
            and dq.date between :startOn and :endOn
            and dq.dailyQuantityAmount.quantityAmount <> 0
        group by tq.totalQuantityId
        order by tq.totalQuantityId asc
    """)
    List<QuantityCompletionAmountProjection> findTotalQuantityCompletionAmountsByUserIdAndDateBetween(
            Long userId,
            LocalDate startOn,
            LocalDate endOn
    );

    @Query("""
        select
            tq.totalQuantityId as totalQuantityId,
            dq.date as date,
            sum(dq.dailyQuantityAmount.quantityAmount) as quantityAmount,
            sum(dq.dailyQuantityAmount.completedQuantityAmount) as completedQuantityAmount
        from DailyQuantityEntity dq
        join dq.weeklyQuantity wq
        join wq.totalQuantity tq
        join tq.sessionalPlan sp
        where sp.user.userId = :userId
            and dq.date between :startOn and :endOn
            and dq.dailyQuantityAmount.quantityAmount <> 0
        group by tq.totalQuantityId, dq.date
    """)
    List<TotalQuantityDailyCompletionAmountProjection> findTotalQuantityDailyCompletionAmountsByUserIdAndDateBetween(
            Long userId,
            LocalDate startOn,
            LocalDate endOn
    );

    interface QuantityCompletionAmountProjection {

        long getQuantityAmount();

        long getCompletedQuantityAmount();
    }

    interface TotalQuantityDailyCompletionAmountProjection extends QuantityCompletionAmountProjection {

        long getTotalQuantityId();

        LocalDate getDate();
    }
}
