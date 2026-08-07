package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;

public interface GetSessionalPlanTotalQuantityUseCase {

    GetSessionalPlanResult.TotalQuantityResult get(GetSessionalPlanTotalQuantityQuery query);
}
