package net.akive.api.plan.application.provided.dailyplan;

import net.akive.api.plan.application.provided.dailyplan.dto.DailyPlansFindQuery;
import net.akive.api.plan.application.result.DailyPlan;

import java.util.List;

public interface DailyPlanFinder {

    List<DailyPlan> find(DailyPlansFindQuery query);
}
