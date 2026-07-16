package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.GetSessionalPlanDetailCommand;
import net.akive.api.plan.application.dto.result.sessionalplan.GetSessionalPlanDetailResult;

public interface GetSessionalPlanDetailUseCase {

    GetSessionalPlanDetailResult getDetail(GetSessionalPlanDetailCommand command);
}