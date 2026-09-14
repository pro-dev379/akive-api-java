package net.akive.api.plan.application.required;

import net.akive.api.plan.domain.model.TotalQuantity;

import java.util.List;
import java.util.Optional;

public interface TotalQuantityRepository {

    TotalQuantity save(TotalQuantity totalQuantity);

    List<TotalQuantity> findAllBySessionalPlanIdOrderByTotalQuantityIdAsc(Long sessionalPlanId);

    Optional<TotalQuantity> findBySessionalPlanIdAndUserIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long userId,
            Long totalQuantityId
    );

    boolean existsBySessionalPlanId(Long sessionalPlanId);

    boolean existsBySessionalPlanIdAndSubjectName(Long sessionalPlanId, String subjectName);

    boolean existsBySessionalPlanIdAndSubjectNameAndTotalQuantityIdNot(
            Long sessionalPlanId,
            String subjectName,
            Long totalQuantityId
    );

    void delete(TotalQuantity totalQuantity);

    long countBySessionalPlanId(Long sessionalPlanId);
}
