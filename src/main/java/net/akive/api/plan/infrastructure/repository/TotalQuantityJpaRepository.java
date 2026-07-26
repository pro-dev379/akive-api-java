package net.akive.api.plan.infrastructure.repository;

import net.akive.api.plan.infrastructure.entity.TotalQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TotalQuantityJpaRepository extends JpaRepository<TotalQuantityEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
        delete from TotalQuantityEntity tq
        where tq.totalQuantityId = :totalQuantityId
          and tq.sessionalPlan.sessionalPlanId = :sessionalPlanId
          and tq.sessionalPlan.user.userId = :userId
        """)
    long deleteByTotalQuantityIdAndSessionalPlanIdAndUserId(Long totalQuantityId, Long sessionalPlanId, Long userId);

    boolean existsBySessionalPlan_SessionalPlanId(Long sessionalPlanId);
    boolean existsBySessionalPlan_SessionalPlanIdAndSubject_Name(Long sessionalPlanId, String subjectName);
    long countBySessionalPlan_SessionalPlanId(Long sessionalPlan);
}
