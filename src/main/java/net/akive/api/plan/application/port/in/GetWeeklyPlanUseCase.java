package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.query.GetWeeklyPlanQuery;
import net.akive.api.plan.application.dto.result.weeklyplan.GetWeeklyPlanResult;

public interface GetWeeklyPlanUseCase {

    GetWeeklyPlanResult getByWeekNum(GetWeeklyPlanQuery query);
}