package net.akive.api.plan.application.provided;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.plan.application.provided.dto.command.CreateSessionalPlanCommand;

public interface CreateSessionalPlanUseCase {

    Long create(CreateSessionalPlanCommand command) throws BadRequestException, ConflictException;
}
