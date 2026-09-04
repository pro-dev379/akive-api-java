package net.akive.api.plan.application.provided.weeklyplan;

import net.akive.api.common.exception.business.ResourceNotFoundException;
import net.akive.api.plan.application.provided.weeklyplan.dto.WeeklyPlanFindQuery;
import net.akive.api.plan.application.result.WeeklyPlan;

public interface WeeklyPlanFinder {

    WeeklyPlan find(WeeklyPlanFindQuery query) throws ResourceNotFoundException;
}
