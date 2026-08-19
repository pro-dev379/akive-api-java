package net.akive.api.plan.application.provided;

import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.dto.command.DeleteSessionalPlanCommand;

public interface DeleteSessionalPlanUseCase {

    void delete(DeleteSessionalPlanCommand command) throws NotFoundException;
}
