package net.akive.api.plan.apapter.persistence;

import net.akive.api.plan.apapter.persistence.projection.SessionalPlanTotalQuantityProjection;

import java.util.List;

public interface SessionalPlanQueryRepository {

    List<SessionalPlanTotalQuantityProjection> findSessionalPlanTotalQuantitiesBySessionalPlanId(Long sessionalPlanId);
    List<SessionalPlanTotalQuantityProjection> findSessionalPlanTotalQuantitiesBySessionalPlanIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long totalQuantityId
    );
}
