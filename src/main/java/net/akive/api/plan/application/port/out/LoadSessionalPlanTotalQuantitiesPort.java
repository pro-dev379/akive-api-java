package net.akive.api.plan.application.port.out;

import net.akive.api.plan.application.port.out.dto.SessionalPlanTotalQuantityData;
import net.akive.api.plan.domain.SessionWeek;

import java.util.List;

public interface LoadSessionalPlanTotalQuantitiesPort {

    List<SessionalPlanTotalQuantityData> loadSessionalPlanTotalQuantities(
            Long sessionalPlanId,
            List<SessionWeek> sessionWeeks
    );
}