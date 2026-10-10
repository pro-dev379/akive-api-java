package net.akive.api.plan.adapter.persistence.totalquantity;

import net.akive.api.plan.adapter.persistence.entity.TotalQuantityEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface TotalQuantityJpaRepository extends JpaRepository<TotalQuantityEntity, Long> {

    @EntityGraph(attributePaths = {"sessionalPlan", "sessionalPlan.user"})
    List<TotalQuantityEntity> findBySessionalPlan_SessionalPlanIdOrderByTotalQuantityIdAsc(Long sessionalPlanId);

    @Query("""
        select tq
        from TotalQuantityEntity tq
        join fetch tq.sessionalPlan sp
        join fetch sp.user u
        where sp.sessionalPlanId = :sessionalPlanId
            and u.userId = :userId
            and tq.totalQuantityId = :totalQuantityId
    """)
    Optional<TotalQuantityEntity> findBySessionalPlanIdAndUserIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long userId,
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
