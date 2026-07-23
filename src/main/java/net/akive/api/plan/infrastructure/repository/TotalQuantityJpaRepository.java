package net.akive.api.plan.infrastructure.repository;

import net.akive.api.plan.infrastructure.entity.TotalQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TotalQuantityJpaRepository extends JpaRepository<TotalQuantityEntity, Long> {

    boolean existsBySessionalPlan_SessionalPlanId(Long sessionalPlanId);
    boolean existsBySessionalPlan_SessionalPlanIdAndSubject_Name(Long sessionalPlanId, String subjectName);
    long countBySessionalPlan_SessionalPlanId(Long sessionalPlan);
}
