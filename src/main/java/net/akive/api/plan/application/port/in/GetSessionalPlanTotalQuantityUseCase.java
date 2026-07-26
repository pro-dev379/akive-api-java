package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanResult;

public interface GetSessionalPlanTotalQuantityUseCase {

    GetSessionalPlanResult.TotalQuantityResult get(GetSessionalPlanTotalQuantityQuery query);
}
