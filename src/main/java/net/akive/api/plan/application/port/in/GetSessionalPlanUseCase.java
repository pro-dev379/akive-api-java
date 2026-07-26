package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.query.GetSessionalPlanQuery;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanResult;

public interface GetSessionalPlanUseCase {

    GetSessionalPlanResult get(GetSessionalPlanQuery query);
}