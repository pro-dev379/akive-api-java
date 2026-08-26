package net.akive.api.plan.application.required;

import net.akive.api.plan.domain.model.TotalQuantity;

import java.util.List;
import java.util.Optional;

public interface TotalQuantityRepository {

    TotalQuantity save(TotalQuantity totalQuantity);

    List<TotalQuantity> findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(Long sessionalPlanId);

    Optional<TotalQuantity> findBySessionalPlanIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long totalQuantityId
    );

    boolean existsBySessionalPlanId(Long sessionalPlanId);

    boolean existsBySessionalPlanIdAndSubjectName(Long sessionalPlanId, String subjectName);

    void deleteByTotalQuantityIdAndSessionalPlanId(Long totalQuantityId, Long sessionalPlanId);

    long countBySessionalPlanId(Long sessionalPlanId);
}
