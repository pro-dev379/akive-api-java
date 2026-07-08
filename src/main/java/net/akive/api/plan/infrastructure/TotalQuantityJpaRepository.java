package net.akive.api.plan.infrastructure;

import net.akive.api.plan.infrastructure.entity.TotalQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TotalQuantityJpaRepository extends JpaRepository<TotalQuantityEntity, Long> {

    boolean existsBySessionalPlan_sessionalPlanId(Long sessionalPlanId);
}
