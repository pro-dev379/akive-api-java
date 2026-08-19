package net.akive.api.plan.application.provided;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanTotalQuantityQuery;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;

public interface GetSessionalPlanTotalQuantityUseCase {

    GetSessionalPlanResult.TotalQuantityResult get(GetSessionalPlanTotalQuantityQuery query) throws NotFoundException;
}
