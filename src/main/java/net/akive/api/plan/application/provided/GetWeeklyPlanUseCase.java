package net.akive.api.plan.application.provided;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.dto.query.GetWeeklyPlanQuery;
import net.akive.api.plan.application.provided.dto.result.weeklyplan.GetWeeklyPlanResult;

public interface GetWeeklyPlanUseCase {

    GetWeeklyPlanResult getByWeekNum(GetWeeklyPlanQuery query) throws NotFoundException;
}
