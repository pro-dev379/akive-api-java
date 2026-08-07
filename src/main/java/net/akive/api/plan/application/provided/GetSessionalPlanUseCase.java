package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanQuery;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;

public interface GetSessionalPlanUseCase {

    GetSessionalPlanResult get(GetSessionalPlanQuery query);
}
