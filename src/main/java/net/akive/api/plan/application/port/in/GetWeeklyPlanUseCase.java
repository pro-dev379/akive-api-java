package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.GetWeeklyPlanCommand;
import net.akive.api.plan.application.dto.result.weeklyplan.GetWeeklyPlanResult;

public interface GetWeeklyPlanUseCase {

    GetWeeklyPlanResult getByWeekNum(GetWeeklyPlanCommand command);
}