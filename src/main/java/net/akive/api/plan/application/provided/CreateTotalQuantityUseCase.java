package net.akive.api.plan.application.provided;

import net.akive.api.common.exception.business.BadRequestException;
import net.akive.api.common.exception.business.ConflictException;
import net.akive.api.common.exception.business.NotFoundException;
import net.akive.api.plan.application.provided.dto.command.CreateTotalQuantityCommand;

public interface CreateTotalQuantityUseCase {

    Long create(CreateTotalQuantityCommand command) throws BadRequestException, ConflictException, NotFoundException;
}
