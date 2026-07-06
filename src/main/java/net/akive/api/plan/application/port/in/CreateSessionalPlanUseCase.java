package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.SessionalPlanCreateCommand;
import net.akive.api.plan.application.dto.result.SessionalPlanSummaryResult;

public interface CreateSessionalPlanUseCase {

    SessionalPlanSummaryResult create(SessionalPlanCreateCommand command);
}
