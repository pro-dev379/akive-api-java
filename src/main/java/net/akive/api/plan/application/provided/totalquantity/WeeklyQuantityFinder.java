package net.akive.api.plan.application.provided.totalquantity;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.totalquantity.dto.WeeklyPlanItemFindQuery;
import net.akive.api.plan.application.result.WeeklyPlan;

public interface WeeklyQuantityFinder {

    WeeklyPlan.WeeklyPlanItem findWeeklyPlanItem(WeeklyPlanItemFindQuery query) throws ResourceNotFoundException;
}
