package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

public interface CreateSessionalPlanUseCase {

    SessionalPlanSummaryResult create(CreateSessionalPlanCommand command);
}
