package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.DeleteSessionalPlanCommand;

public interface DeleteSessionalPlanUseCase {

    void delete(DeleteSessionalPlanCommand command);
}
