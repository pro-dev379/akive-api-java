package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.CreateSessionalPlanCommand;

public interface CreateSessionalPlanUseCase {

    Long create(CreateSessionalPlanCommand command);
}
