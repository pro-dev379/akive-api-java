package net.akive.api.plan.application.provided;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.dto.query.GetSessionalPlanQuery;
import net.akive.api.plan.application.provided.dto.result.sessionalplan.GetSessionalPlanResult;

public interface GetSessionalPlanUseCase {

    GetSessionalPlanResult get(GetSessionalPlanQuery query) throws NotFoundException;
}
