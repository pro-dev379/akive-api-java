package net.akive.api.plan.infrastructure.repository;

import net.akive.api.plan.infrastructure.projection.SessionalPlanTotalQuantityProjection;

import java.util.List;

public interface SessionalPlanQueryRepository {

    List<SessionalPlanTotalQuantityProjection> findSessionalPlanTotalQuantitiesBySessionalPlanId(Long sessionalPlanId);
    List<SessionalPlanTotalQuantityProjection> findSessionalPlanTotalQuantitiesBySessionalPlanIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long totalQuantityId
    );
}
