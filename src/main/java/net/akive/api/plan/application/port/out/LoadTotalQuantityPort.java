package net.akive.api.plan.application.port.out;

import net.akive.api.plan.application.port.out.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.domain.SessionWeek;

import java.util.List;
import java.util.Optional;

public interface LoadTotalQuantityPort {

    List<SessionalPlanTotalQuantityData> loadSessionalPlanTotalQuantitiesBySessionalPlanId(
            Long sessionalPlanId,
            List<SessionWeek> sessionWeeks
    );

    Optional<SessionalPlanTotalQuantityData> loadSessionalPlanTotalQuantityBySessionalPlanIdAndTotalQuantityId(
            Long sessionalPlanId,
            Long totalQuantityId,
            List<SessionWeek> sessionWeeks
    );
}
