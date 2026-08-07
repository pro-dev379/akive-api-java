package net.akive.api.plan.application.provided;

import net.akive.api.plan.application.provided.dto.command.CreateTotalQuantityCommand;

public interface CreateTotalQuantityUseCase {

    Long create(CreateTotalQuantityCommand command);
}
