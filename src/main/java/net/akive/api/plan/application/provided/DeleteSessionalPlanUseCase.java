package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.command.DeleteSessionalPlanCommand;

public interface DeleteSessionalPlanUseCase {

    void delete(DeleteSessionalPlanCommand command);
}
