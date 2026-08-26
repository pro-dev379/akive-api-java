package net.akive.api.plan.adapter.persistence.totalquantity;

import net.akive.api.plan.adapter.persistence.entity.TotalQuantityEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TotalQuantityJpaRepository extends JpaRepository<TotalQuantityEntity, Long> {

    List<TotalQuantityEntity> findBySessionalPlan_SessionalPlanId(Long sessionalPlanId, Sort sort);

    Optional<TotalQuantityEntity> findByTotalQuantityIdAndSessionalPlan_SessionalPlanId(
            Long totalQuantityId,
            Long sessionalPlanId
    );

    Optional<TotalQuantityEntity> findBySessionalPlan_SessionalPlanIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long totalQuantityId
    );

    boolean existsBySessionalPlan_SessionalPlanId(Long sessionalPlanId);

    boolean existsBySessionalPlan_SessionalPlanIdAndSubject_Name(
            Long sessionalPlanId,
            String subjectName
    );

    boolean existsBySessionalPlan_SessionalPlanIdAndSubject_NameAndTotalQuantityIdNot(
            Long sessionalPlanId,
            String subjectName,
            Long totalQuantityId
    );

    long countBySessionalPlan_SessionalPlanId(Long sessionalPlan);
}
