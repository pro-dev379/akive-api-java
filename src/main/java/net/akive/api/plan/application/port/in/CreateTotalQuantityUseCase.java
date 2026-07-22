package net.akive.api.plan.application.port.in;

import net.akive.api.plan.application.dto.command.CreateTotalQuantityCommand;

public interface CreateTotalQuantityUseCase {

    Long create(CreateTotalQuantityCommand command);
}