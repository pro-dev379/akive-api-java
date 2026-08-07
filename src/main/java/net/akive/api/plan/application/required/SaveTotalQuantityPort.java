package net.akive.api.plan.application.required;

import net.akive.api.plan.domain.TotalQuantityPlan;

public interface SaveTotalQuantityPort {

    Long save(TotalQuantityPlan totalQuantityPlan);
}
